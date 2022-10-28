package org.dice_research.rdf.stream.map;

import java.util.function.Consumer;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;

/**
 * A simple class that sends all {@link Node}s of the streamed triples to a
 * given {@link Consumer}.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class RDFStream2NodeStreamMapper extends StreamRDFBase {

    private static final int SUBJECT_FLAG = 1;
    private static final int PREDICATE_FLAG = 2;
    private static final int OBJECT_FLAG = 4;
    private static final int GRAPH_FLAG = 8;

    private Consumer<Node> nodeConsumer;
    private NodeSelection selection;

    public RDFStream2NodeStreamMapper(Consumer<Node> nodeConsumer) {
        this(NodeSelection.ALL, nodeConsumer);
    }

    public RDFStream2NodeStreamMapper(NodeSelection selection, Consumer<Node> nodeConsumer) {
        super();
        this.nodeConsumer = nodeConsumer;
        this.selection = selection;
    }

    @Override
    public void triple(Triple triple) {
        if ((selection.ordinal() & SUBJECT_FLAG) > 0) {
            nodeConsumer.accept(triple.getSubject());
        }
        if ((selection.ordinal() & PREDICATE_FLAG) > 0) {
            nodeConsumer.accept(triple.getPredicate());
        }
        if ((selection.ordinal() & OBJECT_FLAG) > 0) {
            nodeConsumer.accept(triple.getObject());
        }
    }

    @Override
    public void quad(Quad quad) {
        triple(quad.asTriple());
        if ((selection.ordinal() & GRAPH_FLAG) > 0) {
            nodeConsumer.accept(quad.getGraph());
        }
    }

    public enum NodeSelection {
        /**
         * Nothing will be forwarded.
         */
        NOTHING,
        /**
         * Only subjects of the given triples or quads will be forwarded.
         */
        SUBJECT,
        /**
         * Only predicates of the given triples or quads will be forwarded.
         */
        PREDICATE,
        /**
         * Subjects and predicates of the given triples or quads will be forwarded.
         */
        SUBJECT_PREDICATE,
        /**
         * Only objects of the given triples or quads will be forwarded.
         */
        OBJECT,
        /**
         * Subjects and objects of the given triples or quads will be forwarded.
         */
        SUBJECT_OBJECT,
        /**
         * Predicates and objects of the given triples or quads will be forwarded.
         */
        PREDICATE_OBJECT,
        /**
         * All elements of the triples will be forwarded. The graph IRI of quads will
         * not be forwarded.
         */
        TRIPLE, GRAPH, SUBJECT_GRAPH, PREDICATE_GRAPH, SUBJECT_PREDICATE_GRAPH, OBJECT_GRAPH, SUBJECT_OBJECT_GRAPH,
        PREDICATE_OBJECT_GRAPH, ALL;
    }
}
