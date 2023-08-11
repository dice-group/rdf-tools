package org.dice_research.sparql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class offers some helpful methods to load queries from a resource or to
 * get an update query from model differences.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class SparqlQueryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlQueryUtils.class);
    /**
     * Default value of the maximum triples a single update query should have.
     */
    private static final int DEFAULT_MAX_UPDATE_QUERY_TRIPLES = 200;
    /**
     * An empty RDF model instance. It is only used internally for read-only
     * operations.
     */
    private static final Model EMPTY_MODEL = ModelFactory.createDefaultModel();

    /**
     * Loads the given resource, e.g., a SPARQL query, as String.
     *
     * @param loader       the class loader that should be used to access the
     *                     resource
     * @param resourceName name of the resource that should be loaded
     * @param charset      the charset that should be used to read the query from
     *                     the resource
     * @return the resource as String or <code>null</code> if an error occurs
     */
    public static final String loadQuery(ClassLoader loader, String resourceName, Charset charset) {
        InputStream is = loader.getResourceAsStream(resourceName);
        if (is != null) {
            try {
                return IOUtils.toString(is, charset);
            } catch (IOException e) {
                LOGGER.error("Couldn't read query from resource \"" + resourceName + "\". Returning null.");
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            LOGGER.error("Couldn't find needed resource \"" + resourceName + "\". Returning null.");
        }
        return null;
    }

    /**
     * Loads the given resource, e.g., a SPARQL query, as
     * {@link ParameterizedSparqlString}.
     *
     * @param loader       the class loader that should be used to access the
     *                     resource
     * @param resourceName name of the resource that should be loaded
     * @param charset      the charset that should be used to read the query from
     *                     the resource
     * @return the resource as String or <code>null</code> if an error occurs
     */
    public static final ParameterizedSparqlString loadParameterizedQuery(ClassLoader loader, String resourceName,
            Charset charset) {
        String query = loadQuery(loader, resourceName, charset);
        return query == null ? null : new ParameterizedSparqlString(query);
    }

    /**
     * Extends a SPARQL query by inserting specified extension string every time a
     * target string is found within the WHERE clause of the query.
     *
     * @param query     The original query.
     * @param target    Target string to find.
     * @param extension Extension string to insert.
     * @return the modified query or <code>null</code> if the query is invalid.
     */
    public static final String extendQuery(String query, String target, String extension) {
        StringBuilder queryBuilder = new StringBuilder();
        int pos = query.indexOf("WHERE");
        if (pos < 0) {
            return null;
        }
        // Add everything before the WHERE
        queryBuilder.append(query.subSequence(0, pos));
        int oldpos = pos;
        // For every selection triple, insert the extension in front of it
        pos = query.indexOf(target, oldpos);
        while (pos > 0) {
            queryBuilder.append(query.substring(oldpos, pos));
            queryBuilder.append(extension);
            oldpos = pos;
            pos = query.indexOf(target, oldpos + target.length());
        }
        queryBuilder.append(query.substring(oldpos));
        return queryBuilder.toString();
    }

    /**
     * Replaces the given place holders in the given query with the given
     * replacements. If a replacement is <code>null</code>, it is replaced by a
     * variable.
     *
     * @param query        the query containing place holders
     * @param placeholders the place holders that should be replaced
     * @param replacements the replacements that should be used to replace the place
     *                     holders.
     * @return the newly created query or <code>null</code> if the given query was
     *         <code>null</code>.
     * 
     * @deprecated We recommend to use the {@link ParameterizedSparqlString} instead
     *             (e.g., by loading the query with
     *             {@link #loadParameterizedQuery(ClassLoader, String, Charset)}).
     */
    @Deprecated
    public static final String replacePlaceholders(String query, String[] placeholders, String[] replacements) {
        if (query == null) {
            return null;
        }
        if (placeholders.length != replacements.length) {
            throw new IllegalArgumentException("The length of the placeholders != length of replacements.");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < replacements.length; ++i) {
            if (replacements[i] == null) {
                // create a variable name
                builder.append("?v");
                builder.append(i);
            } else if (replacements[i].charAt(0) == '"') {
                // create literal
                builder.append(replacements[i]);
            } else {
                // create <URI>
                builder.append('<');
                builder.append(replacements[i]);
                builder.append('>');
            }
            replacements[i] = builder.toString();
            builder.delete(0, builder.length());
        }
        return StringUtils.replaceEachRepeatedly(query, placeholders, replacements);
    }

    /**
     * Generates a SPARQL UPDATE query based on the differences between the two
     * given models. Triples that are present in the original model but not in the
     * updated model will be put into the DELETE part of the query. Triples that are
     * present in the updated model but can not be found in the original model will
     * be put into the INSERT part of the query.
     *
     * <p>
     * <b>Note</b> that some stores might have a maximum number of triples that can
     * be processed with a single query. In these cases
     * {@link #getUpdateQueriesFromDiff(Model, Model, String, int)} should be used.
     * </p>
     *
     * @param original the original RDF model
     * @param updated  the updated RDF model
     * @param graphUri the URI of the graph to which the UPDATE query should be
     *                 applied or <code>null</code>
     * @return The SPARQL UPDATE query
     */
    public static final String getUpdateQueryFromDiff(Model original, Model updated, String graphUri) {
        return getUpdateQueryFromStatements(original.difference(updated).listStatements().toList(),
                updated.difference(original).listStatements().toList(),
                original.size() > updated.size() ? original : updated, graphUri);
    }

    /**
     * Generates a SPARQL UPDATE query based on the given list of statements that
     * should be deleted and that should be added in the graph with the given URI.
     *
     * @param deleted  statements that should be deleted from the graph
     * @param inserted statements that should be added to the graph
     * @param mapping  A prefix mapping used for the query
     * @param graphUri the URI of the graph which should be updated with the
     *                 generated query
     * @return the update query
     */
    public static final String getUpdateQueryFromStatements(List<Statement> deleted, List<Statement> inserted,
            PrefixMapping mapping, String graphUri) {
        UpdateDeleteInsert update = new UpdateDeleteInsert();
        Node graph = null;
        if (graphUri != null) {
            graph = NodeFactory.createURI(graphUri);
            update.setWithIRI(graph);
        }
        Iterator<Statement> iterator;

        // deleted statements
        iterator = deleted.iterator();
        QuadAcc quads = update.getDeleteAcc();
        while (iterator.hasNext()) {
            quads.addTriple(iterator.next().asTriple());
        }

        // inserted statements
        iterator = inserted.iterator();
        quads = update.getInsertAcc();
        while (iterator.hasNext()) {
            quads.addTriple(iterator.next().asTriple());
        }

        UpdateRequest request = UpdateFactory.create();
        request.add(update);
        return request.toString();
    }

    /**
     * Generates one or several SPARQL UPDATE queries based on the differences
     * between the two given models. Triples that are present in the original model
     * but not in the updated model will be put into the DELETE part of the query.
     * Triples that are present in the updated model but can not be found in the
     * original model will be put into the INSERT part of the query. The changes
     * might be carried out using multiple queries if a single query could hit a
     * maximum number of triples.
     *
     * @param original the original RDF model ({@code null} is interpreted as an
     *                 empty model)
     * @param updated  the updated RDF model ({@code null} is interpreted as an
     *                 empty model)
     * @param graphUri the URI of the graph to which the UPDATE query should be
     *                 applied or <code>null</code>
     * @return The SPARQL UPDATE query
     */
    public static final String[] getUpdateQueriesFromDiff(Model original, Model updated, String graphUri) {
        return getUpdateQueriesFromDiff(original, updated, graphUri, DEFAULT_MAX_UPDATE_QUERY_TRIPLES);
    }

    /**
     * Generates one or several SPARQL UPDATE queries based on the differences
     * between the two given models. Triples that are present in the original model
     * but not in the updated model will be put into the DELETE part of the query.
     * Triples that are present in the updated model but can not be found in the
     * original model will be put into the INSERT part of the query. The changes
     * will be carried out using multiple queries if a single query would hit the
     * given maximum number of triples per query.
     *
     * @param original           the original RDF model ({@code null} is interpreted
     *                           as an empty model)
     * @param updated            the updated RDF model ({@code null} is interpreted
     *                           as an empty model)
     * @param graphUri           the URI of the graph to which the UPDATE query
     *                           should be applied or <code>null</code>
     * @param maxTriplesPerQuery the maximum number of triples a single query should
     *                           contain
     * @return The SPARQL UPDATE query
     */
    public static final String[] getUpdateQueriesFromDiff(Model original, Model updated, String graphUri,
            int maxTriplesPerQuery) {
        if (original == null) {
            original = EMPTY_MODEL;
        }
        if (updated == null) {
            updated = EMPTY_MODEL;
        }
        List<Statement> deleted = original.difference(updated).listStatements().toList();
        List<Statement> inserted = updated.difference(original).listStatements().toList();

        int numberOfDelStmts = deleted.size();
        int totalSize = Math.toIntExact(numberOfDelStmts + inserted.size());
        int queries = (totalSize / maxTriplesPerQuery) + 1;
        String[] results = new String[queries];
        int startIndex = 0;
        int endIndex = Math.min(maxTriplesPerQuery, totalSize);
        List<Statement> delStatements, addStatements;
        List<Statement> emptyList = new ArrayList<>(0);
        for (int i = 0; i < queries; i++) {
            // If we can fill the next query with deleted statements
            if (endIndex < numberOfDelStmts) {
                delStatements = deleted.subList(startIndex, endIndex);
                addStatements = emptyList;
            } else {
                if (startIndex < numberOfDelStmts) {
                    delStatements = deleted.subList(startIndex, numberOfDelStmts);
                    addStatements = inserted.subList(0, endIndex - numberOfDelStmts);
                } else {
                    delStatements = emptyList;
                    addStatements = inserted.subList(startIndex - numberOfDelStmts, endIndex - numberOfDelStmts);
                }
            }
            String query = getUpdateQueryFromStatements(delStatements, addStatements,
                    original.size() > updated.size() ? original : updated, graphUri);
            results[i] = query;
            // get the indexes of the next query
            startIndex = endIndex;
            endIndex = Math.min(endIndex + maxTriplesPerQuery, totalSize);
        }

        return results;
    }
}
