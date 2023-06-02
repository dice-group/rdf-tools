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

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/**
 * <p>
 * This test class tests SPARQL UPDATE queries. It is suggested to extend this
 * class and use the Parameterized JUnit runner to create test cases.
 * </p>
 * <p>
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
public class UpdateQueryTest extends AbstractQueryTest {

    private String graphUri;

    public UpdateQueryTest(String graphIri1, String graphIri2, String storeContentResource, Lang storeContentLang,
            String expectedResultResource, Lang expectedResultLang, String[] queries) {
        super(graphIri1, graphIri2, storeContentResource, storeContentLang, expectedResultResource, expectedResultLang,
                queries);
        this.graphUri = graphIri1;
    }

    @Override
    protected Model executeQueries(String[] queries, Dataset storeContent) {
        DatasetGraph dg = storeContent.asDatasetGraph();
        for (int i = 0; i < queries.length; ++i) {
            UpdateRequest update = UpdateFactory.create(queries[i]);
            UpdateProcessor up = UpdateExecutionFactory.create(update, dg);
            up.execute();
            dg = up.getDatasetGraph();
        }
        return ModelFactory.createModelForGraph(dg.getGraph(NodeFactory.createURI(graphUri)));
    }

}
