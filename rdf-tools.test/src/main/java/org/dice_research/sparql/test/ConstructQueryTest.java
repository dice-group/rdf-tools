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

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;

/**
 * <p>
 * This test class tests SPARQL CONSTRUCT queries. It is suggested to extend
 * this class and use the Parameterized JUnit runner to create test cases.
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
public class ConstructQueryTest extends AbstractQueryTest {

    public ConstructQueryTest(String graphIri1, String graphIri2, String storeContentResource, Lang storeContentLang,
            String expectedResultResource, Lang expectedResultLang, String query) {
        super(graphIri1, graphIri2, storeContentResource, storeContentLang, expectedResultResource, expectedResultLang,
                new String[] { query });
    }

    public ConstructQueryTest(String storeContentResource, Lang storeContentLang, String expectedResultResource,
            Lang expectedResultLang, String query) {
        super(storeContentResource, storeContentLang, expectedResultResource, expectedResultLang,
                new String[] { query });
    }

    @Override
    protected Model executeQueries(String[] queries, Dataset storeContent) {
        QueryExecution qe = QueryExecutionFactory.create(queries[0], storeContent);
        return qe.execConstruct();
    }

}
