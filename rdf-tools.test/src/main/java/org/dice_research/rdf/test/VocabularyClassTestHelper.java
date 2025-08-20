package org.dice_research.rdf.test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.junit.Assert;

/**
 * This class compares a given Java representing an ontology with an RDF model
 * defining the same ontology. The comparison fails if one of the following
 * errors occur:
 * <ol>
 * <li>The class defines a static attribute with the type {@link Resource} or
 * {@link Property} that are not defined in the given RDF model.</li>
 * <li>The given RDF model defines a {@link Resource} or {@link Property} that
 * is not defined as a static attribute of the class or has the wrong type.</li>
 * </ol>
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class VocabularyClassTestHelper {

    protected static OntModel toOntModel(Model readModel) {
        OntModel ontModel = ModelFactory.createOntologyModel();
        ontModel.add(readModel);
        return ontModel;
    }

    /**
     * Executes the comparison.
     * 
     * @param readModel  The RDF model defining the vocabulary
     * @param vocabClazz The Java class implementing the vocabulary
     * @param vocabIri   The IRI of the vocabulary.
     * @throws AssertionError in case the comparison fails
     */
    public static void runTest(Model readModel, Class<?> vocabClazz, String vocabIri) throws AssertionError {
        runTest(toOntModel(readModel), vocabClazz, vocabIri);
    }

    /**
     * Executes the comparison.
     * 
     * @param readModel  The RDF model defining the vocabulary
     * @param vocabClazz The Java class implementing the vocabulary
     * @param vocabIri   The IRI of the vocabulary.
     * @param ignoreCase If <code>true</code>, the case of the attributes and local
     *                   names of IRIs is ignored.
     * @throws AssertionError in case the comparison fails
     */
    public static void runTest(Model readModel, Class<?> vocabClazz, String vocabIri, boolean ignoreCase)
            throws AssertionError {
        runTest(toOntModel(readModel), vocabClazz, vocabIri, ignoreCase);
    }

    /**
     * Executes the comparison.
     * 
     * @param readModel  The RDF model defining the vocabulary
     * @param vocabClazz The Java class implementing the vocabulary
     * @param vocabIri   The IRI of the vocabulary.
     * @param ignoreCase If <code>true</code>, the case of the attributes and local
     *                   names of IRIs is ignored.
     * @param nameFilter A set of class attributes or local names in the RDF model
     *                   that should be ignored during the comparison.
     * @throws AssertionError in case the comparison fails
     */
    public static void runTest(Model readModel, Class<?> vocabClazz, String vocabIri, boolean ignoreCase,
            Set<String> nameFilter) throws AssertionError {
        runTest(toOntModel(readModel), vocabClazz, vocabIri, ignoreCase, nameFilter);
    }

    public static void runTest(OntModel ontModel, Class<?> vocabClazz, String vocabIri) throws AssertionError {
        runTest(ontModel, vocabClazz, vocabIri, false);
    }

    @SuppressWarnings("unchecked")
    public static void runTest(OntModel ontModel, Class<?> vocabClazz, String vocabIri, boolean ignoreCase)
            throws AssertionError {
        runTest(ontModel, vocabClazz, vocabIri, ignoreCase, Collections.EMPTY_SET);
    }

    public static void runTest(OntModel ontModel, Class<?> vocabClazz, String vocabIri, boolean ignoreCase,
            Set<String> nameFilter) throws AssertionError {
        StringBuilder errorMsg = new StringBuilder();
        Set<String> localFilter;
        if (ignoreCase) {
            localFilter = nameFilter.stream().map(n -> n.toLowerCase()).collect(Collectors.toSet());
        } else {
            localFilter = nameFilter;
        }
        Map<String, Field> clazzAttributes = getClassAttributes(vocabClazz, ignoreCase, localFilter);
        Set<String> expectedPropertyNames = collectDefinedElements(ontModel, true, vocabIri, localFilter, ignoreCase);
        compare(expectedPropertyNames, clazzAttributes, Property.class, errorMsg);

        // Filter the property names that we already checked before
        localFilter = new HashSet<String>(localFilter);
        localFilter.addAll(expectedPropertyNames);
        // Add an empty resource for the vocabulary IRI
        localFilter.add("");
        Set<String> expectedResourceNames = collectDefinedElements(ontModel, false, vocabIri, localFilter, ignoreCase);
        compare(expectedResourceNames, clazzAttributes, Resource.class, errorMsg);

        Assert.assertTrue(errorMsg.toString(), errorMsg.length() == 0);
    }

    protected static void compare(Set<String> expectedNames, Map<String, Field> clazzAttributes, Class<?> expectedType,
            StringBuilder errorMsg) {
        // Ensure that all expected elements are listed as class attribute
        for (String name : expectedNames) {
            if (clazzAttributes.containsKey(name)) {
                // Ensure that all expected elements have the correct class
                if (!expectedType.equals(clazzAttributes.get(name).getType())) {
                    errorMsg.append("Element with local name \"");
                    errorMsg.append(name);
                    errorMsg.append("\" has the type \"");
                    errorMsg.append(clazzAttributes.get(name).getType().getName());
                    errorMsg.append("\" instead of the expected type \"");
                    errorMsg.append(expectedType.getName());
                    errorMsg.append("\".");
                }
            } else {
                errorMsg.append("Element with local name \"");
                errorMsg.append(name);
                errorMsg.append("\" is missing. ");
            }
        }

        // Get class attributes with the given type that are not in the list of expected
        // elements
        clazzAttributes.entrySet().stream().filter(e -> expectedType.equals(e.getValue().getType()))
                .filter(e -> !expectedNames.contains(e.getKey())).forEach(e -> {
                    errorMsg.append("Element with local name \"");
                    errorMsg.append(e.getKey());
                    errorMsg.append("\" and type  \"");
                    errorMsg.append(expectedType.getName());
                    errorMsg.append("\" is defined in the class but was not expected.");
                });
    }

    protected static Set<String> collectDefinedElements(OntModel ontModel, boolean collectProperties, String vocabIri,
            Set<String> nameFilter, boolean ignoreCase) {
        Stream<String> iriStream;
        if (collectProperties) {
            iriStream = ontModel.listAllOntProperties().toSet().stream().map(p -> p.getURI());
        } else {
            iriStream = ontModel.listSubjects().toSet().stream().filter(r -> r.isURIResource()).map(r -> r.getURI());
        }
        // Only keep IRIs of the target name space and remove the name space after that
        iriStream = iriStream.filter(iri -> iri.startsWith(vocabIri)).map(iri -> iri.substring(vocabIri.length()));
        // Transform the local names if the case doesn't matter
        if (ignoreCase)
            iriStream = iriStream.map(iri -> iri.toLowerCase());
        // Filter based on the given filter and transform the result into a set
        return iriStream.filter(iri -> !nameFilter.contains(iri)).collect(Collectors.toSet());
    }

    protected static Map<String, Field> getClassAttributes(Class<?> vocabClazz, boolean ignoreCase,
            Set<String> nameFilter) {
        return Arrays.stream(vocabClazz.getFields())
                // Filter based on given set
                .filter(ignoreCase ? f -> !nameFilter.contains(f.getName().toLowerCase())
                        : f -> !nameFilter.contains(f.getName()))
                // Collect to map name -> field
                .collect(Collectors.toMap(ignoreCase ? f -> f.getName().toLowerCase() : f -> f.getName(),
                        Function.identity()));
    }
}
