package org.dice_research.rdf.stream.filter.node;

import java.util.function.Predicate;

import org.apache.jena.graph.Node;

/**
 * Abstract implementation of a Node Filter which offers the usage of a return
 * value. If the filter implementation returns
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public abstract class ANodeFilter implements Predicate<Node> {

    /**
     * Default return value if the check of the filter returns true.
     */
    public static final boolean DEFAULT_RETURN_VALUE = true;

    /**
     * The value that is returned in case the two nodes are equal. Else, its inverse
     * is returned.
     */
    protected boolean returnValue;

    /**
     * Constructor that uses the {@link #DEFAULT_RETURN_VALUE}.
     */
    public ANodeFilter() {
        this.returnValue = DEFAULT_RETURN_VALUE;
    }

    /**
     * Constructor.
     * 
     * @param returnValue The value that is returned in case the two nodes are
     *                    equal. Else, its inverse is returned.
     */
    public ANodeFilter(boolean returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public boolean test(Node t) {
        return check(t) ? returnValue : !returnValue;
    }

    /**
     * The internal method that is called to check a given {@link Node} for its
     * validity.
     * 
     * @param n the node that should be checked
     * @return true if the node is valid with respect to the filter's internal
     *         implementation; else false.
     */
    protected abstract boolean check(Node n);
}
