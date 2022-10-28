package org.dice_research.rdf.stream;

import org.apache.jena.riot.system.StreamRDF;

/**
 * A decorator of a {@link StreamRDF} object. It acts as a {@link StreamRDF}
 * object and may or may not forward calls to the {@link StreamRDF} object that
 * it wraps.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface StreamRDFDecorator extends StreamRDF {

    /**
     * Returns the decorated {@link StreamRDF} object.
     * 
     * @return the decorated object
     */
    public StreamRDF getDecorated();
}
