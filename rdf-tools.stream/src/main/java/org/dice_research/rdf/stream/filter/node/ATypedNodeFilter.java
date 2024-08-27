package org.dice_research.rdf.stream.filter.node;

import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Node_Variable;

/**
 * Abstract implementation of a Node Filter which offers single methods for the
 * different node types. All methods come with a default implementation that
 * returns {@code true}.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public abstract class ATypedNodeFilter extends ANodeFilter implements Predicate<Node> {

    public static boolean DEFAULT_RETURN_VALUE_UNCOVERED_TYPES = false;

    /**
     * The value that is used as check result for types that are not covered by
     * implementations of this abstract class. If this is {@code true}
     * {@link ANodeFilter#returnValue} is returned for nodes that are handled by the
     * default implementations. If it is set to {@code false} the negation of
     * {@link ANodeFilter#returnValue} is returned.
     */
    protected boolean returnValueUncoveredTypes;

    /**
     * Constructor that uses the {@link #DEFAULT_RETURN_VALUE} and
     * {@link DEFAULT_RETURN_VALUE_UNCOVERED_TYPES} =
     * {@value DEFAULT_RETURN_VALUE_UNCOVERED_TYPES} for node types for which the
     * check method is not overridden.
     */
    public ATypedNodeFilter() {
        this(DEFAULT_RETURN_VALUE, DEFAULT_RETURN_VALUE_UNCOVERED_TYPES);
    }

    /**
     * Constructor. By default, it returns
     * {@link DEFAULT_RETURN_VALUE_UNCOVERED_TYPES} =
     * {@value DEFAULT_RETURN_VALUE_UNCOVERED_TYPES} for node types for which the
     * check method is not overridden.
     * 
     * @param returnValue The value that is returned in case the two nodes are
     *                    equal. Else, its inverse is returned.
     */
    public ATypedNodeFilter(boolean returnValue) {
        this(returnValue, DEFAULT_RETURN_VALUE_UNCOVERED_TYPES);
    }

    /**
     * Constructor.
     * 
     * @param returnValue               The value that is returned in case the test
     *                                  succeeds. Else, its inverse is returned.
     * @param returnValueUncoveredTypes The value that is used as check result for
     *                                  types that are not covered by
     *                                  implementations of this abstract class or if
     *                                  the node to check is {@code null}.
     */
    public ATypedNodeFilter(boolean returnValue, boolean returnValueUncoveredTypes) {
        super(returnValue, returnValueUncoveredTypes);
        if(returnValue) {
            this.returnValueUncoveredTypes = returnValueUncoveredTypes;
        } else {
            // We have to invert this value, since it will be inverted later on again...
            this.returnValueUncoveredTypes = !returnValueUncoveredTypes;
        }
    }

    protected boolean check(Node n) {
        if (n.isBlank()) {
            return checkBlank((Node_Blank) n);
//        } else if (n.isNodeGraph()) {
//            return checkGraphNode((Node_URI) n);
        } else if (n.isURI()) {
            return checkURI((Node_URI) n);
        } else if (n.isVariable()) {
            return checkVariable((Node_Variable) n);
        } else if (n.isLiteral()) {
            return checkLiteral((Node_Literal) n);
        } else if (n.isNodeTriple()) {
            return checkTriple((Node_Triple) n);
        } else {
            throw new IllegalStateException(
                    "Got an unknown node type: \"" + n.getClass().getCanonicalName() + "\". Aborting.");
        }
    }

    /**
     * A check method that is guaranteed to be called only with blank nodes.
     * 
     * @param n the blank node that should be checked
     * @return true if the node is valid with respect to the filter's internal
     *         implementation; else false.
     */
    protected boolean checkBlank(Node_Blank n) {
        return returnValueUncoveredTypes;
    }

//    /**
//     * A check method that is guaranteed to be called only for nodes that represent
//     * the graph IRI of a quadruple.
//     * 
//     * @param n the graph IRI node that should be checked
//     * @return true if the node is valid with respect to the filter's internal
//     *         implementation; else false.
//     */
//    protected boolean checkGraphNode(Node_URI n) {
//        return returnValueUncoveredTypes;
//    }

    /**
     * A check method that is guaranteed to be called only for nodes that represent
     * an IRI. Note that for graph IRIs of a quadruple {@link #checkNodeGraph} is
     * called.
     * 
     * @param n the graph IRI node that should be checked
     * @return true if the node is valid with respect to the filter's internal
     *         implementation; else false.
     */
    protected boolean checkURI(Node_URI n) {
        return returnValueUncoveredTypes;
    }

    /**
     * A check method that is guaranteed to be called only with variable nodes.
     * 
     * @param n the variable node that should be checked
     * @return true if the node is valid with respect to the filter's internal
     *         implementation; else false.
     */
    protected boolean checkVariable(Node_Variable n) {
        return returnValueUncoveredTypes;
    }

    /**
     * A check method that is guaranteed to be called only with literal nodes.
     * 
     * @param n the literal node that should be checked
     * @return true if the node is valid with respect to the filter's internal
     *         implementation; else false.
     */
    protected boolean checkLiteral(Node_Literal n) {
        return returnValueUncoveredTypes;
    }

    /**
     * A check method that is guaranteed to be called only with a node that
     * represents a triple (within RDF*).
     * 
     * @param n the node that represents a triple and that should be checked
     * @return true if the node is valid with respect to the filter's internal
     *         implementation; else false.
     */
    protected boolean checkTriple(Node_Triple n) {
        return returnValueUncoveredTypes;
    }
}
