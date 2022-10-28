package org.dice_research.rdf.stream.filter;

import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class NodeFilterBasedTripleFilter implements Predicate<Triple> {

    protected Predicate<Node> subjectCheck;
    protected Predicate<Node> predicateCheck;
    protected Predicate<Node> objectCheck;

    public NodeFilterBasedTripleFilter(Predicate<Node> subjectCheck, Predicate<Node> predicateCheck,
            Predicate<Node> objectCheck) {
        super();
        this.subjectCheck = subjectCheck;
        this.predicateCheck = predicateCheck;
        this.objectCheck = objectCheck;
    }

    @Override
    public boolean test(Triple t) {
        return (subjectCheck == null || subjectCheck.test(t.getSubject()))
                & (predicateCheck == null || predicateCheck.test(t.getPredicate()))
                && (objectCheck == null || objectCheck.test(t.getObject()));
    }

    // FIXME Add builder to create this filter in an easier way (i.e., accept
    // String... for single positions, put null in positions that are not set, etc.)
}
