package org.dice_group.rdf.stream.util;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.lang.SinkTriplesToGraph;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.dice_research.rdf.stream.util.StreamUtils;
import org.dice_research.rdf.test.ModelComparisonHelper;
import org.junit.Test;

public class StreamUtilsTest {

    @Test
    public void writeReifiedStatementTest() {
        Resource subject = ResourceFactory.createResource("http://example.org/subject");
        Property predicate = ResourceFactory.createProperty("http://example.org/predicate");
        Resource object = ResourceFactory.createResource("http://example.org/object");
        Resource stmtRes = ResourceFactory.createResource("http://test.org/statement-1");
        
        Model streamedModel = ModelFactory.createDefaultModel();
        Sink<Triple> sink = new SinkTriplesToGraph(false, streamedModel.getGraph());
        try {
            StreamRDF stream = StreamRDFLib.sinkTriples(sink);

            stream.start();
            StreamUtils.writeReifiedStatement(stmtRes.asNode(), subject.asNode(), predicate.asNode(), object.asNode(),
                    stream);
            stream.finish();
        } finally {
            sink.close();
        }

        Statement stmt = ResourceFactory.createStatement(subject, predicate, object);
        Model expectedModel = ModelFactory.createDefaultModel();
        expectedModel.createReifiedStatement(stmtRes.getURI(), stmt);
        
        ModelComparisonHelper.assertModelsEqual(expectedModel, streamedModel);
    }
}
