package org.dice_research.rdf.stream;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

public abstract class AStreamRDFDecorator implements StreamRDFDecorator {

    private StreamRDF decorated;

    public AStreamRDFDecorator(StreamRDF decorated) {
        this.decorated = decorated;
    }

    @Override
    public StreamRDF getDecorated() {
        return decorated;
    }

    @Override
    public void start() {
        decorated.start();
    }

    @Override
    public void triple(Triple triple) {
        decorated.triple(triple);
    }

    @Override
    public void quad(Quad quad) {
        decorated.quad(quad);
    }

    @Override
    public void base(String base) {
        decorated.base(base);
    }

    @Override
    public void prefix(String prefix, String iri) {
        decorated.prefix(prefix, iri);
    }

    @Override
    public void finish() {
        decorated.finish();
    }

}
