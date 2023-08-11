package org.dice_research.sparql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.dice_research.rdf.test.ModelResourceUtils;
import org.dice_research.sparql.test.UpdateQueryTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SparqlQueryUtilsUpdateFromDiffTest extends UpdateQueryTest {

    public SparqlQueryUtilsUpdateFromDiffTest(String graphIri1, String graphIri2, String storeContentResource,
            Lang storeContentLang, String expectedResultResource, Lang expectedResultLang, String[] queries) {
        super(graphIri1, graphIri2, storeContentResource, storeContentLang, expectedResultResource, expectedResultLang,
                queries);
    }

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        List<Object[]> testConfigs = new ArrayList<Object[]>();

        // Test examples migrated from the HOBBIT project

        final String FIRST_GRAPH_NAME = "http://graph.org/1";
        final String SECOND_GRAPH_NAME = "http://graph.org/2";

        // Check the model diff based SPARQL UPDATE query creation
        Model original, updated;
        original = ModelResourceUtils.loadModel(SparqlQueryUtilsUpdateFromDiffTest.class.getClassLoader(),
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE);
        updated = ModelResourceUtils.loadModel(SparqlQueryUtilsUpdateFromDiffTest.class.getClassLoader(),
                "org/dice_research/sparql/changedChallengeConfig.ttl", Lang.TURTLE);
        /*
         * The original model is changed to the updated model as expected.
         */
        testConfigs.add(new Object[] { FIRST_GRAPH_NAME, SECOND_GRAPH_NAME,
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE,
                "org/dice_research/sparql/changedChallengeConfig.ttl", Lang.TURTLE,
                new String[] { SparqlQueryUtils.getUpdateQueryFromDiff(original, updated, FIRST_GRAPH_NAME) } });
        /*
         * A query that should focus on the second graph does not change the first graph
         */
        testConfigs.add(new Object[] { FIRST_GRAPH_NAME, SECOND_GRAPH_NAME,
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE,
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE,
                new String[] { SparqlQueryUtils.getUpdateQueryFromDiff(original, updated, SECOND_GRAPH_NAME) } });
        /*
         * A query that should DELETE and INSERT something does not change an empty
         * graph.
         */
        testConfigs.add(new Object[] { FIRST_GRAPH_NAME, SECOND_GRAPH_NAME, null, null, null, null,
                new String[] { SparqlQueryUtils.getUpdateQueryFromDiff(original, updated, FIRST_GRAPH_NAME) } });
        /*
         * The original model is changed to the updated model as expected with the
         * possibility to create multiple queries.
         */
        testConfigs.add(new Object[] { FIRST_GRAPH_NAME, SECOND_GRAPH_NAME,
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE,
                "org/dice_research/sparql/changedChallengeConfig.ttl", Lang.TURTLE,
                SparqlQueryUtils.getUpdateQueriesFromDiff(original, updated, FIRST_GRAPH_NAME) });
        /*
         * The original model is changed to the updated model as expected with one
         * triple per query
         */
        testConfigs.add(new Object[] { FIRST_GRAPH_NAME, SECOND_GRAPH_NAME,
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE,
                "org/dice_research/sparql/changedChallengeConfig.ttl", Lang.TURTLE,
                SparqlQueryUtils.getUpdateQueriesFromDiff(original, updated, FIRST_GRAPH_NAME, 1) });
        /*
         * The original model is changed to the updated model as expected with up to two
         * triples per query
         */
        testConfigs.add(new Object[] { FIRST_GRAPH_NAME, SECOND_GRAPH_NAME,
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE,
                "org/dice_research/sparql/changedChallengeConfig.ttl", Lang.TURTLE,
                SparqlQueryUtils.getUpdateQueriesFromDiff(original, updated, FIRST_GRAPH_NAME, 2) });
        /*
         * A query that should focus on the second graph does not change the first graph
         */
        testConfigs.add(new Object[] { FIRST_GRAPH_NAME, SECOND_GRAPH_NAME,
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE,
                "org/dice_research/sparql/exampleChallengeConfig.ttl", Lang.TURTLE,
                SparqlQueryUtils.getUpdateQueriesFromDiff(original, updated, SECOND_GRAPH_NAME) });

        return testConfigs;
    }
}
