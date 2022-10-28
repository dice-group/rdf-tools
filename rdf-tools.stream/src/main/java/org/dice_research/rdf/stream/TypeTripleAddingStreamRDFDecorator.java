package org.dice_research.rdf.stream;

import org.apache.jena.riot.system.StreamRDF;

public class TypeTripleAddingStreamRDFDecorator extends AStreamRDFDecorator {

    public TypeTripleAddingStreamRDFDecorator(StreamRDF decorated) {
        super(decorated);
    }

    
}
