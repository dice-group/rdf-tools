/**
 * This file is part of core.
 *
 * core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with core.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dice_research.sparql.test;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.dice_research.rdf.test.ModelComparisonHelper;
import org.dice_research.rdf.test.ModelResourceUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * Abstract test class that loads a named graph that serves as storage content
 * and an RDF model that contains the expected result, applies a given query
 * (using the abstract {@link #executeQuery(String, Dataset)} method) to the
 * storage content and compares the result with the expected result. The name of
 * the graph containing the data loaded from a given resource is defined by
 * {@link #graphIri1}. The storage contains a second, empty graph with a name
 * defined by {@link #graphIri2}.
 * </p>
 *
 * <p>
 * If one of the given resource names is null, the named graph in the storage or
 * the result model will be empty.
 * </p>
 * 
 * <p>
 * Parts of this class origin from the
 * <a href="https://github.com/hobbit-project/core">core library</a> of the
 * HOBBIT project.
 * </p>
 *
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public abstract class AbstractQueryTest {

    protected static final String FIRST_GRAPH_NAME = "http://example.org/graph1";
    protected static final String SECOND_GRAPH_NAME = "http://example.org/graph2";

    /**
     * The IRI of the graph in which the storage content will be loaded.
     */
    protected String graphIri1;
    /**
     * The IRI of a second, empty graph.
     */
    protected String graphIri2;
    /**
     * Name of the resource from which the store content is loaded.
     */
    protected String storeContentResource;
    /**
     * The RDF serialization language which has been used to store the store
     * content.
     */
    protected Lang storeContentLang;
    /**
     * Name of the resource from which the expected result is loaded.
     */
    protected String expectedResultResource;
    /**
     * The RDF serialization language which has been used to store the expected
     * result.
     */
    protected Lang expectedResultLang;
    /**
     * SPARQL query/queries that is executed on the store content to create the
     * expected result model.
     */
    protected String[] queries;

    /**
     * Constructor. Makes use of the default graph IRIs {@link #FIRST_GRAPH_NAME}
     * and {@link #SECOND_GRAPH_NAME}.
     * 
     * @param storeContentResource   Name of the resource from which the store
     *                               content is loaded.
     * @param storeContentLang       The RDF serialization language which has been
     *                               used to store the store content.
     * @param expectedResultResource Name of the resource from which the expected
     *                               result is loaded.
     * @param expectedResultLang     The RDF serialization language which has been
     *                               used to store the expected result.
     * @param queries                SPARQL query/queries that is executed on the
     *                               store content to create the expected result
     *                               model.
     */
    public AbstractQueryTest(String storeContentResource, Lang storeContentLang, String expectedResultResource,
            Lang expectedResultLang, String... queries) {
        this(FIRST_GRAPH_NAME, SECOND_GRAPH_NAME, storeContentResource, storeContentLang, expectedResultResource,
                expectedResultLang, queries);
    }

    /**
     * Constructor.
     * 
     * @param graphIri1              The IRI of the graph in which the storage
     *                               content will be loaded.
     * @param graphIri2              The IRI of a second, empty graph.
     * @param storeContentResource   Name of the resource from which the store
     *                               content is loaded.
     * @param storeContentLang       The RDF serialization language which has been
     *                               used to store the store content.
     * @param expectedResultResource Name of the resource from which the expected
     *                               result is loaded.
     * @param expectedResultLang     The RDF serialization language which has been
     *                               used to store the expected result.
     * @param queries                SPARQL query/queries that is executed on the
     *                               store content to create the expected result
     *                               model.
     */
    public AbstractQueryTest(String graphIri1, String graphIri2, String storeContentResource, Lang storeContentLang,
            String expectedResultResource, Lang expectedResultLang, String... queries) {
        super();
        this.graphIri1 = graphIri1;
        this.graphIri2 = graphIri2;
        this.storeContentResource = storeContentResource;
        this.storeContentLang = storeContentLang;
        this.queries = queries;
        this.expectedResultResource = expectedResultResource;
        this.expectedResultLang = expectedResultLang;
    }

    protected abstract Model executeQueries(String[] queries, Dataset storeContent);

    @Test
    public void test() {
        // Make sure the queries have been loaded correctly
        Assert.assertNotNull(queries);
        for (int i = 0; i < queries.length; ++i) {
            Assert.assertNotNull(queries[i]);
        }
        // load the models
        Dataset storeContent = DatasetFactory.createTxnMem();
        // If the named graph is not empty, load it
        if (storeContentResource != null) {
            storeContent.addNamedModel(graphIri1, ModelResourceUtils.loadModel(this.getClass().getClassLoader(),
                    storeContentResource, storeContentLang));
        } else {
            storeContent.addNamedModel(graphIri1, ModelFactory.createDefaultModel());
        }
        storeContent.addNamedModel(graphIri2, ModelFactory.createDefaultModel());
        // load/create expected result
        Model expectedResult = null;
        if (expectedResultResource != null) {
            expectedResult = ModelResourceUtils.loadModel(this.getClass().getClassLoader(), expectedResultResource,
                    expectedResultLang);
        } else {
            // an empty result is expected
            expectedResult = ModelFactory.createDefaultModel();
        }

        // execute query
        Model result = executeQueries(queries, storeContent);

        // Compare the models
        String expectedModelString = expectedResult.toString();
        String resultModelString = result.toString();
        // Check the precision and recall
        Set<Statement> missingStatements = ModelComparisonHelper.getMissingStatements(expectedResult, result);
        Set<Statement> unexpectedStatements = ModelComparisonHelper.getMissingStatements(result, expectedResult);

        StringBuilder builder = new StringBuilder();
        if (unexpectedStatements.size() != 0) {
            builder.append("The result contains the unexpected statements:\n\n"
                    + unexpectedStatements.stream().map(Object::toString).collect(Collectors.joining("\n"))
                    + "\n\nExpected model:\n\n" + expectedModelString + "\nResult model:\n\n" + resultModelString
                    + "\n");
        }
        if (missingStatements.size() != 0) {
            builder.append("The result does not contain the expected statements:\n\n"
                    + missingStatements.stream().map(Object::toString).collect(Collectors.joining("\n"))
                    + "\n\nExpected model:\n\n" + expectedModelString + "\n\nResult model:\n\n" + resultModelString
                    + "\n");
        }

        Assert.assertTrue(builder.toString(), missingStatements.size() == 0 && unexpectedStatements.size() == 0);
    }

}
