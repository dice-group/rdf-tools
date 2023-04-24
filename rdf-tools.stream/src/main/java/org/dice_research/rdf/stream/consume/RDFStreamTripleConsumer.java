package org.dice_research.rdf.stream.consume;

import java.util.function.Consumer;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFApply;

/**
 * This class can be used to let the given {@link Consumer} consume triples from
 * an RDF stream. This class is actually just a simplification of the
 * {@link StreamRDFApply} class that transforms all quads in a stream to triples
 * before applying the given consumer.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class RDFStreamTripleConsumer extends StreamRDFApply {

    /**
     * Constructor.
     * 
     * @param consumer the {@link Consumer} that is applied to every triple on the
     *                 stream.
     */
    public RDFStreamTripleConsumer(Consumer<Triple> consumer) {
        super(consumer, q -> consumer.accept(q.asTriple()));
    }
}
