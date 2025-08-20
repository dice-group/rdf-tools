package org.dice_research.rdf.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class VocabularyClassTestHelperTest {

    private Model model;
    private Set<String> filter;
    private boolean comparisonPasses;
    private boolean comparisonPassesWhenCaseIgnored;
    private boolean comparisonPassesWhenFiltered;
    private boolean comparisonPassesWhenFilteredAndCaseIgnored;

    public VocabularyClassTestHelperTest(Model model, Set<String> filter, boolean comparisonPasses,
            boolean comparisonPassesWhenCaseIgnored, boolean comparisonPassesWhenFiltered,
            boolean comparisonPassesWhenFilteredAndCaseIgnored) {
        super();
        this.model = model;
        this.filter = filter;
        this.comparisonPasses = comparisonPasses;
        this.comparisonPassesWhenCaseIgnored = comparisonPassesWhenCaseIgnored;
        this.comparisonPassesWhenFiltered = comparisonPassesWhenFiltered;
        this.comparisonPassesWhenFilteredAndCaseIgnored = comparisonPassesWhenFilteredAndCaseIgnored;
    }

    @Test
    public void test() {
        try {
            VocabularyClassTestHelper.runTest(model, TestVocbulary.class, TestVocbulary.uri);
        } catch (java.lang.AssertionError e) {
            // This is expected, so we can exit
            if (comparisonPasses) {
                throw e;
            } else {
                return;
            }
        }
        if (!comparisonPasses) {
            Assert.fail("There is an error that hasn't been detected");
        }
    }

    @Test
    public void testIgnoreCase() {
        try {
            VocabularyClassTestHelper.runTest(model, TestVocbulary.class, TestVocbulary.uri, true);
        } catch (java.lang.AssertionError e) {
            // This is expected, so we can exit
            if (comparisonPassesWhenCaseIgnored) {
                throw e;
            } else {
                return;
            }
        }
        if (!comparisonPassesWhenCaseIgnored) {
            Assert.fail("There is an error that hasn't been detected");
        }
    }

    @Test
    public void testFilter() {
        try {
            VocabularyClassTestHelper.runTest(model, TestVocbulary.class, TestVocbulary.uri, false, filter);
        } catch (java.lang.AssertionError e) {
            // This is expected, so we can exit
            if (comparisonPassesWhenFiltered) {
                throw e;
            } else {
                return;
            }
        }
        if (!comparisonPassesWhenFiltered) {
            Assert.fail("There is an error that hasn't been detected");
        }
    }

    @Test
    public void testIgnoreCaseFilter() {
        try {
            VocabularyClassTestHelper.runTest(model, TestVocbulary.class, TestVocbulary.uri, true, filter);
        } catch (java.lang.AssertionError e) {
            // This is expected, so we can exit
            if (comparisonPassesWhenFilteredAndCaseIgnored) {
                throw e;
            } else {
                return;
            }
        }
        if (!comparisonPassesWhenFilteredAndCaseIgnored) {
            Assert.fail("There is an error that hasn't been detected");
        }
    }

    @Parameters
    public static List<Object[]> parameters() {
        List<Object[]> testCases = new ArrayList<>();
        Model model;

        // Test with a vocab without any issues.
        testCases.add(new Object[] { TestVocbulary.getAsModel(), Collections.EMPTY_SET, true, true, true, true });

        // Test vocabulary that is correct if the case is ignored
        model = TestVocbulary.getAsModel();
        model.remove(TestVocbulary.exampleProp1, RDF.type, OWL.ObjectProperty);
        model.add(model.createResource(TestVocbulary.getURI() + "EXAMPLEPROP1"), RDF.type, OWL.ObjectProperty);
        testCases.add(new Object[] { model, Collections.EMPTY_SET, false, true, false, true });
        // The same with a filter for "EXAMPLEPROP1", "exampleProp1"
        testCases.add(new Object[] { model, Set.of("EXAMPLEPROP1", "exampleProp1"), false, true, true, true });

        // Test property missing in class
        model = TestVocbulary.getAsModel();
        model.add(model.createResource(TestVocbulary.getURI() + "newTestProperty"), RDF.type, OWL.ObjectProperty);
        testCases.add(new Object[] { model, Collections.EMPTY_SET, false, false, false, false });
        // The same with a filter for the missing class
        testCases.add(new Object[] { model, Set.of("newTestProperty"), false, false, true, true });

        // Text property missing in Ontology
        model = TestVocbulary.getAsModel();
        model.remove(TestVocbulary.exampleProp1, RDF.type, OWL.ObjectProperty);
        testCases.add(new Object[] { model, Collections.EMPTY_SET, false, false, false, false });
        // The same with a filter for the missing class
        testCases.add(new Object[] { model, Set.of("exampleProp1"), false, false, true, true });

        // Test resource missing in class
        model = TestVocbulary.getAsModel();
        model.add(model.createResource(TestVocbulary.getURI() + "NewResource"), RDF.type, OWL2.NamedIndividual);
        testCases.add(new Object[] { model, Collections.EMPTY_SET, false, false, false, false });
        // The same with a filter for the missing class
        testCases.add(new Object[] { model, Set.of("NewResource"), false, false, true, true });

        // Test resource missing in ontology
        model = TestVocbulary.getAsModel();
        model.remove(TestVocbulary.ExampleResource1, RDF.type, OWL2.NamedIndividual);
        testCases.add(new Object[] { model, Collections.EMPTY_SET, false, false, false, false });
        // The same with a filter for the missing class
        testCases.add(new Object[] { model, Set.of("ExampleResource1"), false, false, true, true });

        // Test resource defined as property
        model = TestVocbulary.getAsModel();
        // Remove the property definition
        model.remove(TestVocbulary.exampleProp1, RDF.type, OWL.ObjectProperty);
        // Define it as resource
        model.remove(TestVocbulary.exampleProp1, RDF.type, OWL2.NamedIndividual);
        testCases.add(new Object[] { model, Collections.EMPTY_SET, false, false, false, false });
        // The same with a filter for the missing class
        testCases.add(new Object[] { model, Set.of("exampleProp1"), false, false, true, true });

        // Test property defined as resource
        model = TestVocbulary.getAsModel();
        // Remove the resource definition
        model.remove(TestVocbulary.ExampleResource2, RDF.type, OWL2.NamedIndividual);
        // Define it as property
        model.remove(TestVocbulary.ExampleResource2, RDF.type, OWL.ObjectProperty);
        testCases.add(new Object[] { model, Collections.EMPTY_SET, false, false, false, false });
        // The same with a filter for the missing class
        testCases.add(new Object[] { model, Set.of("ExampleResource2"), false, false, true, true });

        return testCases;
    }

    public static class TestVocbulary {

        public static Model getAsModel() {
            Model model = ModelFactory.createDefaultModel();
            model.add(ExampleClass, RDF.type, RDFS.Class);
            model.add(ExampleClass, RDF.type, OWL.Class);
            model.add(ExampleResource1, RDF.type, OWL2.NamedIndividual);
            model.add(ExampleResource2, RDF.type, OWL2.NamedIndividual);
            model.add(exampleProp1, RDF.type, OWL.ObjectProperty);
            model.add(exampleProp2, RDF.type, OWL.DatatypeProperty);
            return model;
        }

        /**
         * The namespace of the vocabulary as a string
         */
        public static final String uri = "http://example.org/test-vocab#";

        // Resources sorted alphabetically
        public static final Resource ExampleClass = resource("ExampleClass");
        public static final Resource ExampleResource1 = resource("ExampleResource1");
        public static final Resource ExampleResource2 = resource("ExampleResource2");

        // Properties sorted alphabetically
        public static final Property exampleProp1 = property("exampleProp1");
        public static final Property exampleProp2 = property("exampleProp2");

        /**
         * returns the URI for this schema
         * 
         * @return the URI for this schema
         */
        public static String getURI() {
            return uri;
        }

        protected static final Resource resource(String local) {
            return ResourceFactory.createResource(uri + local);
        }

        protected static final Property property(String local) {
            return ResourceFactory.createProperty(uri, local);
        }
    }
}
