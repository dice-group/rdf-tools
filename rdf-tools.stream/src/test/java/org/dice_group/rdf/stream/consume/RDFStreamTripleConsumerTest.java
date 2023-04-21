package org.dice_group.rdf.stream.consume;

import java.net.URL;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.dice_research.rdf.stream.consume.RDFStreamTripleConsumer;
import org.dice_research.rdf.test.ModelComparisonHelper;
import org.junit.Assert;
import org.junit.Test;

public class RDFStreamTripleConsumerTest {

    @Test
    public void test() {
        URL exampleUrl = this.getClass().getClassLoader().getResource("Example.nt");
        Assert.assertNotNull(exampleUrl);

        // Let's use a very simple consumer which simply adds a triple to an existing
        // RDF model
        Model readModel = ModelFactory.createDefaultModel();
        StreamRDF stream = new RDFStreamTripleConsumer(t -> readModel.getGraph().add(t));
        RDFDataMgr.parse(stream, exampleUrl.toString(), Lang.NT);

        ModelComparisonHelper.assertModelsEqual(exampleUrl, readModel);
    }
}
