package org.dice_research.rdf.stream.filter.node;

import java.util.function.Predicate;

import org.apache.jena.graph.Node;

/**
 * Abstract implementation of a Node Filter which offers the usage of a return
 * value. If the internal filter implementation returns {@code true},
 * {@link #returnValue} is returned. Else, its inverse is returned.
 * 
 * It also offers a special handling of {@code null} values.
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
     * The value that is returned in case the given node is {@code null}.
     */
    protected boolean returnValueForNull;

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
        this(returnValue, false);
    }

    /**
     * Constructor.
     * 
     * @param returnValue        The value that is returned in case the two nodes
     *                           are equal. Else, its inverse is returned.
     * @param returnValueForNull The value that is returned in case the given node
     *                           is {@code null}.
     */
    public ANodeFilter(boolean returnValue, boolean returnValueForNull) {
        super();
        this.returnValue = returnValue;
        this.returnValueForNull = returnValueForNull;
    }

    @Override
    public boolean test(Node t) {
        if (t == null) {
            return returnValueForNull;
        } else {
            return check(t) ? returnValue : !returnValue;
        }
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

    /**
     * @return the returnValue
     */
    public boolean isReturnValue() {
        return returnValue;
    }

    /**
     * @param returnValue the returnValue to set
     */
    public void setReturnValue(boolean returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * @return the returnValueForNull
     */
    public boolean isReturnValueForNull() {
        return returnValueForNull;
    }

    /**
     * @param returnValueForNull the returnValueForNull to set
     */
    public void setReturnValueForNull(boolean returnValueForNull) {
        this.returnValueForNull = returnValueForNull;
    }
}
