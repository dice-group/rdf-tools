package org.dice_research.rdf.stream;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.Quad;

public abstract class AStreamRDFFilter extends AStreamRDFDecorator {

    private StreamRDF rejected;

    public AStreamRDFFilter(StreamRDF accepted, StreamRDF rejected) {
        super(accepted);
        this.rejected = rejected;
    }

    public AStreamRDFFilter(StreamRDF accepted) {
        this(accepted, StreamRDFLib.sinkNull());
    }

    protected StreamRDF getRejected() {
        return rejected;
    }

    protected abstract boolean filter(Triple triple);

    protected abstract boolean filter(Quad quad);

    @Override
    public void start() {
        super.start();
        rejected.start();
    }

    @Override
    public void triple(Triple triple) {
        if (filter(triple)) {
            super.triple(triple);
        } else {
            rejected.triple(triple);
        }
    }

    @Override
    public void quad(Quad quad) {
        if (filter(quad)) {
            super.quad(quad);
        } else {
            rejected.quad(quad);
        }
    }

    @Override
    public void base(String base) {
        super.base(base);
        rejected.base(base);
    }

    @Override
    public void prefix(String prefix, String iri) {
        super.prefix(prefix, iri);
        rejected.prefix(prefix, iri);
    }

    @Override
    public void finish() {
        super.finish();
        rejected.finish();
    }

}
