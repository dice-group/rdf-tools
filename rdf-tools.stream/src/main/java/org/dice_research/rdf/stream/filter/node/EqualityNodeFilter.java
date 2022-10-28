package org.dice_research.rdf.stream.filter.node;

import java.util.Objects;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;

/**
 * A node filter that returns the given boolean value if the tested node equals
 * the given node. Else, the inverse of the given boolean value is returned.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class EqualityNodeFilter extends ANodeFilter implements Predicate<Node> {

    /**
     * Node that is used for comparison.
     */
    protected Node example;

    /**
     * Constructor. Creates a filter that will return true if the tested node and
     * the given example are equal.
     * 
     * @param example Node that is used for comparison
     */
    public EqualityNodeFilter(Node example) {
        this(example, DEFAULT_RETURN_VALUE);
    }

    /**
     * Constructor.
     * 
     * @param example     Node that is used for comparison.
     * @param returnValue The value that is returned in case the two nodes are
     *                    equal. Else, its inverse is returned
     */
    public EqualityNodeFilter(Node example, boolean returnValue) {
        super(returnValue);
        Objects.requireNonNull(example,
                "The given example node is not allowed to be null since we can not call the equals() method of a null object.");
        this.example = example;
    }

    @Override
    public boolean check(Node n) {
        return example.equals(n);
    }
}
