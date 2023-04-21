package org.dice_research.rdf.stream.consume;

import java.util.function.Consumer;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;

/**
 * This class can be used to let the given {@link Consumer} consume triples from
 * an RDF stream.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class RDFStreamTripleConsumer extends StreamRDFBase {

    private Consumer<Triple> consumer;

    public RDFStreamTripleConsumer(Consumer<Triple> consumer) {
        super();
        this.consumer = consumer;
    }

    @Override
    public void triple(Triple triple) {
        consumer.accept(triple);
    }

    @Override
    public void quad(Quad quad) {
        triple(quad.asTriple());
    }
}
