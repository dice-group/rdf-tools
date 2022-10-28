package org.dice_research.rdf.stream.map;

import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.dice_research.rdf.stream.AStreamRDFDecorator;

public class RDFStreamTripleFlatMapper extends AStreamRDFDecorator {

    protected Function<Triple, Stream<Triple>> tripleFunction;

    public RDFStreamTripleFlatMapper(Function<Triple, Stream<Triple>> tripleFunction, StreamRDF decorated) {
        super(decorated);
        this.tripleFunction = tripleFunction;
    }

    @Override
    public void triple(Triple triple) {
        tripleFunction.apply(triple).forEach(t -> super.triple(t));
    }

    @Override
    public void quad(Quad quad) {
        triple(quad.asTriple());
    }

}
