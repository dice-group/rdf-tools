package org.dice_research.rdf.stream.filter.node;

import org.apache.jena.graph.Node_URI;

/**
 * A node filter that returns the given boolean value ({@code true} by default)
 * if the tested node is an IRI node and has an IRI that starts with one of the
 * given name space IRIs. Else, the inverse of the given boolean value is
 * returned. It should be noted that testing the name space is implemented in a
 * simple String-based way, i.e., the check only tests whether the IRI starts
 * with the name space IRI.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class StringBasedNamespaceNodeFilter extends ATypedNodeFilter {

    /**
     * Node that is used for comparison.
     */
    protected String[] namespaces;

    /**
     * Constructor. Creates a filter that will return true if the tested node and
     * the given example are equal.
     * 
     * @param example Node that is used for comparison
     */
    public StringBasedNamespaceNodeFilter(String... namespaces) {
        this(DEFAULT_RETURN_VALUE, namespaces);
    }

    /**
     * Constructor.
     * 
     * @param example     Node that is used for comparison.
     * @param returnValue The value that is returned in case the two nodes are
     *                    equal. Else, its inverse is returned
     */
    public StringBasedNamespaceNodeFilter(boolean returnValue, String... namespaces) {
        super(returnValue);
        this.namespaces = namespaces;
    }

    @Override
    protected boolean checkURI(Node_URI n) {
        String iri = n.getURI();
        for (int i = 0; i < namespaces.length; ++i) {
            if (iri.startsWith(namespaces[i])) {
                return true;
            }
        }
        return false;
    }

}
