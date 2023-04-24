package org.dice_research.rdf.stream.util;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.vocabulary.RDF;

/**
 * This is a simple utilities class that offers some methods easing the work
 * with {@link StreamRDF} instances.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class StreamUtils {

    /**
     * This method streams a reified RDF statement with the given IRI and the given
     * subject, predicate and object to the given stream.
     * 
     * @param stmt      the IRI or blank node representation of the reified
     *                  statement
     * @param subject   the subject of the reified triple
     * @param predicate the predicate of the reified triple
     * @param object    the object of the reified triple
     * @param stream    the stream to which the generated triples will be sent
     */
    public static void writeReifiedStatement(Node stmt, Node subject, Node predicate, Node object, StreamRDF stream) {
        stream.triple(Triple.create(stmt, RDF.type.asNode(), RDF.Statement.asNode()));
        stream.triple(Triple.create(stmt, RDF.subject.asNode(), subject));
        stream.triple(Triple.create(stmt, RDF.predicate.asNode(), predicate));
        stream.triple(Triple.create(stmt, RDF.object.asNode(), object));
    }
}
