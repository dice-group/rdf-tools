package org.dice_research.rdf.test;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.dice_research.sparql.test.AbstractQueryTest;
import org.junit.Assert;

/**
 * A simple class that loads a given resource as RDF {@link Model} object.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class ModelResourceUtils {

    /**
     * This method loads RDF data from the resource with the given name using the
     * given {@link ClassLoader} into a newly created {@link Model} object.
     * 
     * @param loader       the class loader which is used to access the resource
     * @param resourceName the name of the resource
     * @param rdfLanguage  the RDF language that is used to parse the resource
     * @return the newly created model containing the read RDF data
     * @throws IllegalStateException if there is an error reading the resource
     */
    public static Model loadModel(ClassLoader loader, String resourceName, Lang rdfLanguage) {
        Model model = ModelFactory.createDefaultModel();
        loadModel(model, loader, resourceName, rdfLanguage);
        return model;
    }

    /**
     * This method loads RDF data from the resource with the given name using the
     * given {@link ClassLoader} into the given {@link Model} object.
     * 
     * @param model        the {@link Model} object to which the read RDF data will
     *                     be added
     * @param loader       the class loader which is used to access the resource
     * @param resourceName the name of the resource
     * @param rdfLanguage  the RDF language that is used to parse the resource
     * @throws IllegalStateException if there is an error reading the resource
     */
    public static void loadModel(Model model, ClassLoader loader, String resourceName, Lang rdfLanguage) {
        try (InputStream is = AbstractQueryTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            Assert.assertNotNull("Couldn't load " + resourceName + " as resource.", is);
            RDFDataMgr.read(model, is, rdfLanguage);
        } catch (Throwable t) {
            throw new IllegalStateException("Got an exception while loading model from \"" + resourceName + "\".", t);
        }
    }
}
