package org.dice_research.rdf.stream.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.graph.Triple;

public class PropertyBasedTripleFilter implements Predicate<Triple> {

    /**
     * Set of properties that are accepted as predicate of the triples.
     */
    protected Set<String> acceptedProperties;

    public PropertyBasedTripleFilter(String...acceptedProperties) {
        this(new HashSet<String>(Arrays.asList(acceptedProperties)));
    }

    public PropertyBasedTripleFilter(Set<String> acceptedProperties) {
        Objects.requireNonNull(acceptedProperties, "A filter with null as accepted properties does not work.");
        this.acceptedProperties = acceptedProperties;
    }

    @Override
    public boolean test(Triple t) {
        return acceptedProperties.contains(t.getPredicate().getURI());
    }

}
