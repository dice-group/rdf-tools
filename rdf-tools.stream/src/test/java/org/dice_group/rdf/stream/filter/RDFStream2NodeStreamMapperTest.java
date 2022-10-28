package org.dice_group.rdf.stream.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.graph.Factory;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFOps;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RDFStream2NodeStreamMapperTest {

    @Parameters
    public static List<Object[]> parameters() {
        List<Object[]> testCases = new ArrayList<>();

        // A simple test case that checks for a given predicate
        testCases.add(new Object[] {
                (Predicate<Triple>) (t -> t.getPredicate().getURI().equals("http://example.org/p1")),
                new Triple[] { new Triple(NodeFactory.createURI("http://example.org/e1"),
                        NodeFactory.createURI("http://example.org/p1"), NodeFactory.createURI("http://example.org/e2")),
                        new Triple(NodeFactory.createURI("http://example.org/e1"),
                                NodeFactory.createURI("http://example.org/p2"), NodeFactory.createLiteral("test")),
                        new Triple(NodeFactory.createBlankNode(), NodeFactory.createURI("http://example.org/p1"),
                                NodeFactory.createLiteral("test")),
                        new Triple(NodeFactory.createURI("http://example.org/e1"),
                                NodeFactory.createURI("http://example.org/p2"),
                                NodeFactory.createURI("http://example.org/e2")),
                        new Triple(NodeFactory.createURI("http://example.org/e1"),
                                NodeFactory.createURI("http://example.org/p1"), NodeFactory.createLiteral("test")),
                        new Triple(NodeFactory.createBlankNode(), NodeFactory.createURI("http://example.org/p2"),
                                NodeFactory.createLiteral("test")) },
                new boolean[] { true, false, true, false, true, false } });

        return testCases;
    }

    private Predicate<Triple> predicate;
    private Triple[] triples;
    private boolean[] expected;

    public RDFStream2NodeStreamMapperTest(Predicate<Triple> predicate, Triple[] triples, boolean[] expected) {
        super();
        this.predicate = predicate;
        this.triples = triples;
        this.expected = expected;
    }

    @Test
    public void test() {
        // Create stream with the given data and store the result in resultModel
        Model posModel = ModelFactory.createDefaultModel();
        StreamRDF posStream = StreamRDFLib.graph(posModel.getGraph());
        Model negModel = ModelFactory.createDefaultModel();
        StreamRDF negStream = StreamRDFLib.graph(negModel.getGraph());
        StreamRDF stream = new RDFStreamTripleFilter(predicate, posStream, negStream);
        StreamRDFOps.sendTriplesToStream(Arrays.asList(triples).iterator(), stream);

        // Check the two result models
        String posModelString = posModel.toString();
        String negModelString = negModel.toString();
        ModelCom model4CreatingStmts = new ModelCom(Factory.createGraphMem());
        Statement s;
        for (int i = 0; i < expected.length; ++i) {
            s = StatementImpl.toStatement(triples[i], model4CreatingStmts);
            if (expected[i]) {
                Assert.assertTrue("Triple " + triples[i].toString()
                        + " is not in the positive result model but was expected. Result model: " + posModelString,
                        posModel.contains(s));
                Assert.assertFalse(
                        "Triple " + triples[i].toString()
                                + " was found in the negative result model but was not expected. Result model: "
                                + negModelString,
                        negModel.contains(StatementImpl.toStatement(triples[i], model4CreatingStmts)));
            } else {
                Assert.assertFalse(
                        "Triple " + triples[i].toString()
                                + " was found in the positive result model but was not expected. Result model: "
                                + posModelString,
                        posModel.contains(StatementImpl.toStatement(triples[i], model4CreatingStmts)));
                Assert.assertTrue("Triple " + triples[i].toString()
                        + " is not in the negative result model but was expected. Result model: " + negModelString,
                        negModel.contains(s));
            }
        }
    }
}
