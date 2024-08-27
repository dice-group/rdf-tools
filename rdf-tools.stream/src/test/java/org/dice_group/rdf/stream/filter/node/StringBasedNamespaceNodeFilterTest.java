package org.dice_group.rdf.stream.filter.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.dice_research.rdf.stream.filter.node.StringBasedNamespaceNodeFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class StringBasedNamespaceNodeFilterTest {

    @Parameters
    public static List<Object[]> parameters() {
        List<Object[]> testCases = new ArrayList<>();

        // A simple test case that checks the simplest case: we have a single namespace
        // and no further configuration is done.
        testCases.add(new Object[] { new StringBasedNamespaceNodeFilter("http://example.org/ns1"),
                new Node[] { NodeFactory.createURI("http://example.org/ns1#entity1"),
                        NodeFactory.createURI("http://example.org/ns1"),
                        NodeFactory.createURI("http://example.org/ns2#entity1"),
                        NodeFactory.createURI("http://example.org/ns2"), NodeFactory.createBlankNode(),
                        NodeFactory.createLiteral("http://example.org/ns1"), null },
                new boolean[] { true, true, false, false, false, false, false } });
        // A simple test case with multiple namespaces
        testCases.add(new Object[] { new StringBasedNamespaceNodeFilter("http://example.org/ns1", "http://ex.org/ns3"),
                new Node[] { NodeFactory.createURI("http://example.org/ns1#entity1"),
                        NodeFactory.createURI("http://example.org/ns1"),
                        NodeFactory.createURI("http://example.org/ns2#entity1"),
                        NodeFactory.createURI("http://example.org/ns2"), NodeFactory.createBlankNode(),
                        NodeFactory.createLiteral("http://example.org/ns1"), null,
                        NodeFactory.createURI("http://ex.org/ns3#entity1"),
                        NodeFactory.createURI("http://ex.org/ns3"), },
                new boolean[] { true, true, false, false, false, false, false, true, true } });
        // We repeat it but set the return values explicitly to true
        testCases.add(
                new Object[] { new StringBasedNamespaceNodeFilter(true, "http://example.org/ns1", "http://ex.org/ns3"),
                        new Node[] { NodeFactory.createURI("http://example.org/ns1#entity1"),
                                NodeFactory.createURI("http://example.org/ns1"),
                                NodeFactory.createURI("http://example.org/ns2#entity1"),
                                NodeFactory.createURI("http://example.org/ns2"), NodeFactory.createBlankNode(),
                                NodeFactory.createLiteral("http://example.org/ns1"), null,
                                NodeFactory.createURI("http://ex.org/ns3#entity1"),
                                NodeFactory.createURI("http://ex.org/ns3"), },
                        new boolean[] { true, true, false, false, false, false, false, true, true } });
        // We repeat it but set the return values explicitly to true and false
        testCases.add(new Object[] {
                new StringBasedNamespaceNodeFilter(true, false, "http://example.org/ns1", "http://ex.org/ns3"),
                new Node[] { NodeFactory.createURI("http://example.org/ns1#entity1"),
                        NodeFactory.createURI("http://example.org/ns1"),
                        NodeFactory.createURI("http://example.org/ns2#entity1"),
                        NodeFactory.createURI("http://example.org/ns2"), NodeFactory.createBlankNode(),
                        NodeFactory.createLiteral("http://example.org/ns1"), null,
                        NodeFactory.createURI("http://ex.org/ns3#entity1"),
                        NodeFactory.createURI("http://ex.org/ns3"), },
                new boolean[] { true, true, false, false, false, false, false, true, true } });
        // We inverse the result. This will invert everything
        testCases.add(new Object[] { new StringBasedNamespaceNodeFilter(false, "http://example.org/ns1", "http://ex.org/ns3"),
                new Node[] { NodeFactory.createURI("http://example.org/ns1#entity1"),
                        NodeFactory.createURI("http://example.org/ns1"),
                        NodeFactory.createURI("http://example.org/ns2#entity1"),
                        NodeFactory.createURI("http://example.org/ns2"), NodeFactory.createBlankNode(),
                        NodeFactory.createLiteral("http://example.org/ns1"), null,
                        NodeFactory.createURI("http://ex.org/ns3#entity1"),
                        NodeFactory.createURI("http://ex.org/ns3"), },
                new boolean[] { false, false, true, true, true, true, true, false, false } });
        // We inverse the result and want to let nodes different type pass
        testCases.add(new Object[] { new StringBasedNamespaceNodeFilter(false, true, "http://example.org/ns1", "http://ex.org/ns3"),
                new Node[] { NodeFactory.createURI("http://example.org/ns1#entity1"),
                        NodeFactory.createURI("http://example.org/ns1"),
                        NodeFactory.createURI("http://example.org/ns2#entity1"),
                        NodeFactory.createURI("http://example.org/ns2"), NodeFactory.createBlankNode(),
                        NodeFactory.createLiteral("http://example.org/ns1"), null,
                        NodeFactory.createURI("http://ex.org/ns3#entity1"),
                        NodeFactory.createURI("http://ex.org/ns3"), },
                new boolean[] { false, false, true, true, true, true, true, false, false } });
        // We inverse the result and don't want to let nodes different type pass
        testCases.add(new Object[] { new StringBasedNamespaceNodeFilter(false, false, "http://example.org/ns1", "http://ex.org/ns3"),
                new Node[] { NodeFactory.createURI("http://example.org/ns1#entity1"),
                        NodeFactory.createURI("http://example.org/ns1"),
                        NodeFactory.createURI("http://example.org/ns2#entity1"),
                        NodeFactory.createURI("http://example.org/ns2"), NodeFactory.createBlankNode(),
                        NodeFactory.createLiteral("http://example.org/ns1"), null,
                        NodeFactory.createURI("http://ex.org/ns3#entity1"),
                        NodeFactory.createURI("http://ex.org/ns3"), },
                new boolean[] { false, false, true, true, false, false, false, false, false } });

        return testCases;
    }

    private StringBasedNamespaceNodeFilter filter;
    private Node[] nodes;
    private boolean[] expected;

    public StringBasedNamespaceNodeFilterTest(StringBasedNamespaceNodeFilter filter, Node[] nodes, boolean[] expected) {
        super();
        this.filter = filter;
        this.nodes = nodes;
        this.expected = expected;
    }

    @Test
    public void test() {
        for (int i = 0; i < nodes.length; ++i) {
            String msg = "Didn't get expected result for " + (nodes[i] != null ? nodes[i].toString() : "null");
            if (expected[i]) {
                Assert.assertTrue(msg, filter.test(nodes[i]));
            } else {
                Assert.assertFalse(msg, filter.test(nodes[i]));
            }
        }
    }
}
