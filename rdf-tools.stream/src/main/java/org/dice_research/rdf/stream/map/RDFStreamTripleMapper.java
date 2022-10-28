package org.dice_research.rdf.stream.map;

import java.util.function.Function;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.dice_research.rdf.stream.AStreamRDFDecorator;

public class RDFStreamTripleMapper extends AStreamRDFDecorator {

    protected Function<Triple, Triple> tripleFunction;

    public RDFStreamTripleMapper(Function<Triple, Triple> tripleFunction, StreamRDF decorated) {
        super(decorated);
        this.tripleFunction = tripleFunction;
    }

    @Override
    public void triple(Triple triple) {
        super.triple(tripleFunction.apply(triple));
    }

    @Override
    public void quad(Quad quad) {
        triple(quad.asTriple());
    }

}
