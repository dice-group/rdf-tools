package org.dice_research.rdf.stream.filter;

import java.util.function.Predicate;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.dice_research.rdf.stream.AStreamRDFFilter;

public class RDFStreamTripleFilter extends AStreamRDFFilter {

    protected Predicate<Triple> tripleFilter;

    public RDFStreamTripleFilter(Predicate<Triple> tripleFilter, StreamRDF accepted) {
        super(accepted);
        this.tripleFilter = tripleFilter;
    }

    public RDFStreamTripleFilter(Predicate<Triple> tripleFilter, StreamRDF accepted, StreamRDF rejected) {
        super(accepted, rejected);
        this.tripleFilter = tripleFilter;
    }

    @Override
    protected boolean filter(Triple triple) {
        return tripleFilter.test(triple);
    }

    @Override
    protected boolean filter(Quad quad) {
        return filter(quad.asTriple());
    }

}
