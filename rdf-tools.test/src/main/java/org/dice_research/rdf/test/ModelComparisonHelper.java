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
package org.dice_research.rdf.test;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Assert;

/**
 * A simple utility class that helps comparing two models.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class ModelComparisonHelper {

    /**
     * This method compares the given result model with the given expected model. It
     * uses the {@link Assert} class for the comparison.
     * 
     * @param expected the URL from which the expected RDF model can be loaded
     * @param result   the actual RDF model which should be compared to the expected
     *                 model
     */
    public static void assertModelsEqual(URL expected, Model result) {
        assertModelsEqual(loadModel(expected), result);
    }

    /**
     * This method compares the given result model with the given expected model. It
     * uses the {@link Assert} class for the comparison.
     * 
     * @param expected the expected RDF model
     * @param result   the URL from which the actual RDF model can be loaded
     */
    public static void assertModelsEqual(Model expected, URL result) {
        assertModelsEqual(expected, loadModel(result));
    }

    /**
     * This method compares the given result model with the given expected model. It
     * uses the {@link Assert} class for the comparison.
     * 
     * @param expected the URL from which the expected RDF model can be loaded
     * @param result   the URL from which the actual RDF model can be loaded
     */
    public static void assertModelsEqual(URL expected, URL result) {
        assertModelsEqual(loadModel(expected), loadModel(result));
    }

    /**
     * This method compares the given result model with the given expected model. It
     * uses the {@link Assert} class for the comparison.
     * 
     * @param expected the expected RDF model
     * @param result   the actual RDF model which should be compared to the expected
     *                 model
     */
    public static void assertModelsEqual(Model expected, Model result) {
        Assert.assertNotNull("The given expected model is null.", expected);
        Assert.assertNotNull("The given result model is null.", result);
        // Compare the models
        String expectedModelString = expected.toString();
        String resultModelString = result.toString();
        // Check the precision and recall
        Set<Statement> missingStatements = ModelComparisonHelper.getMissingStatements(expected, result);
        Set<Statement> unexpectedStatements = ModelComparisonHelper.getMissingStatements(result, expected);

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

    /**
     * Collects statements that can be found in model A but not in model B. If A and
     * B are seen as sets of statements, this method returns the difference A\B.
     *
     * @param modelA the model that should be fully contained inside model B.
     * @param modelB the model that should fully contain model A.
     * @return the difference A\B which is empty if A is a subset of B
     */
    public static Set<Statement> getMissingStatements(Model modelA, Model modelB) {
        Set<Statement> statements = new HashSet<>();
        StmtIterator iterator = modelA.listStatements();
        Statement s;
        while (iterator.hasNext()) {
            s = iterator.next();
            if (!modelContainsStatement(modelB, s)) {
                statements.add(s);
            }
        }
        return statements;
    }

    /**
     * Checks whether the given statement can be found in the given model. If the
     * given statement contains blank nodes (= Anon nodes) they are replaced by
     * variables.
     *
     * @param model the model that might contain the given statement
     * @param s     the statement which could be contained in the given model
     * @return <code>true</code> if the statement can be found in the model,
     *         <code>false</code> otherwise
     */
    public static boolean modelContainsStatement(Model model, Statement s) {
        Resource subject = s.getSubject();
        RDFNode object = s.getObject();
        if (subject.isAnon()) {
            if (object.isAnon()) {
                return model.contains(null, s.getPredicate(), (RDFNode) null);
            } else {
                return model.contains(null, s.getPredicate(), object);
            }
        } else {
            if (object.isAnon()) {
                return model.contains(subject, s.getPredicate(), (RDFNode) null);
            } else {
                return model.contains(subject, s.getPredicate(), object);
            }
        }
    }

    /**
     * Tries to load the model from the given URL.
     * 
     * @param url the location of the RDF model that should be loaded
     * @return the model that could be loaded
     */
    public static Model loadModel(URL url) {
        Model model = ModelFactory.createDefaultModel();
        model.read(url.toString());
        return model;
    }

}
