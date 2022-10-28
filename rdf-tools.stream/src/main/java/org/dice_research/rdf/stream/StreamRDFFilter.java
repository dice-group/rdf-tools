package org.dice_research.rdf.stream;

import org.apache.jena.riot.system.StreamRDF;

/**
 * An interface for a {@link StreamRDF} implementation which separates the
 * received stream elements based on a given filter. It is backed up by up to
 * two {@link StreamRDF} instances - one for the elements that are accepted by
 * the filter and a second for stream elements that are rejected by the filter.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public interface StreamRDFFilter extends StreamRDFDecorator {

    /**
     * Returns the decorated {@link StreamRDF} instance which is called for elements
     * of the RDF stream that are accepted by the implemented filter. By default,
     * this method is exactly the same as calling {@link #getDecorated()}.
     * 
     * @return the decorated stream for accepted elements
     */
    public default StreamRDF getAccepted() {
        return getDecorated();
    }

    /**
     * Returns the decorated {@link StreamRDF} instance which is called for elements
     * of the RDF stream that are rejected by the implemented filter. <b>Note</b>
     * that this stream may delete the elements instead of processing them further.
     * 
     * @return the decorated stream for rejected elements
     */
    public StreamRDF getRejected();
}
