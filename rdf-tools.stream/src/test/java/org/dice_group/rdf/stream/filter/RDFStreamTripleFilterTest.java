package org.dice_group.rdf.stream.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFOps;
import org.dice_research.rdf.stream.map.RDFStream2NodeStreamMapper;
import org.dice_research.rdf.stream.map.RDFStream2NodeStreamMapper.NodeSelection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RDFStreamTripleFilterTest {

    @Parameters
    public static List<Object[]> parameters() {
        List<Object[]> testCases = new ArrayList<>();

        Triple[] triples = new Triple[] {
                new Triple(NodeFactory.createURI("http://example.org/e1"),
                        NodeFactory.createURI("http://example.org/p1"), NodeFactory.createURI("http://example.org/e2")),
                new Triple(NodeFactory.createURI("http://example.org/e1"),
                        NodeFactory.createURI("http://example.org/p2"), NodeFactory.createLiteral("test")),
                new Triple(NodeFactory.createBlankNode("a"), NodeFactory.createURI("http://example.org/p1"),
                        NodeFactory.createLiteral("test")),
                new Triple(NodeFactory.createURI("http://example.org/e1"),
                        NodeFactory.createURI("http://example.org/p2"), NodeFactory.createURI("http://example.org/e2")),
                new Triple(NodeFactory.createURI("http://example.org/e1"),
                        NodeFactory.createURI("http://example.org/p1"), NodeFactory.createLiteral("test")),
                new Triple(NodeFactory.createBlankNode("a"), NodeFactory.createURI("http://example.org/p2"),
                        NodeFactory.createLiteral("test")) };

        // NOTING
        testCases.add(new Object[] { triples, NodeSelection.NOTHING, new String[] {}, 0 });

        // SUBJECT
        testCases
                .add(new Object[] { triples, NodeSelection.SUBJECT, new String[] { "http://example.org/e1", "a" }, 6 });

        // PREDICATE
        testCases.add(new Object[] { triples, NodeSelection.PREDICATE,
                new String[] { "http://example.org/p1", "http://example.org/p2" }, 6 });

        // OBJECT
        testCases.add(new Object[] { triples, NodeSelection.OBJECT,
                new String[] { "http://example.org/e2", "\"test\"" }, 6 });

        // SUBJECT_PREDICATE
        testCases.add(new Object[] { triples, NodeSelection.SUBJECT_PREDICATE,
                new String[] { "http://example.org/e1", "http://example.org/p1", "http://example.org/p2", "a" }, 12 });

        // SUBJECT_OBJECT
        testCases.add(new Object[] { triples, NodeSelection.SUBJECT_OBJECT,
                new String[] { "http://example.org/e1", "http://example.org/e2", "\"test\"", "a" }, 12 });

        // PREDICATE_OBJECT
        testCases.add(new Object[] { triples, NodeSelection.PREDICATE_OBJECT,
                new String[] { "http://example.org/e2", "http://example.org/p1", "http://example.org/p2", "\"test\"", },
                12 });

        // ALL
        testCases.add(new Object[] { triples, NodeSelection.ALL, new String[] { "http://example.org/e1",
                "http://example.org/e2", "http://example.org/p1", "http://example.org/p2", "\"test\"", "a" }, 18 });

        return testCases;
    }

    private Triple[] triples;
    private NodeSelection selection;
    private String[] expectedNodes;
    private int expectedNodeCount;

    public RDFStreamTripleFilterTest(Triple[] triples, NodeSelection selection, String[] expectedNodes,
            int expectedNodeCount) {
        super();
        this.triples = triples;
        this.selection = selection;
        this.expectedNodes = expectedNodes;
        this.expectedNodeCount = expectedNodeCount;
    }

    @Test
    public void test() {
        final AtomicInteger counter = new AtomicInteger(0);
        final Set<String> collector = new HashSet<String>();
        StreamRDFOps.sendTriplesToStream(Arrays.asList(triples).iterator(),
                new RDFStream2NodeStreamMapper(selection, n -> {
                    counter.incrementAndGet();
                    collector.add(n.toString());
                }));

        String[] collectedNodes = collector.toArray(String[]::new);
        Arrays.sort(collectedNodes);
        Arrays.sort(expectedNodes);
        Assert.assertArrayEquals(
                "expected " + Arrays.toString(expectedNodes) + " but got " + Arrays.toString(collectedNodes),
                expectedNodes, collectedNodes);

        Assert.assertEquals(expectedNodeCount, counter.longValue());
    }
}
