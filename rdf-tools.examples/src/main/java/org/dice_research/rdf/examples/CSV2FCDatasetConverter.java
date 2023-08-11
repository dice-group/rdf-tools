package org.dice_research.rdf.examples;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.rdf.stream.consume.RDFStreamTripleConsumer;
import org.dice_research.rdf.stream.filter.NodeFilterBasedTripleFilter;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.dice_research.rdf.stream.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This class takes a CSV file (subject, predicate, object, truthvalue) and
 * assigns the read truth value to the statements of the given FC file which
 * have the given subject, predicate, and object.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class CSV2FCDatasetConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSV2FCDatasetConverter.class);

    private static final Resource VERACITY_PROPERTY = ResourceFactory
            .createResource("http://swc2017.aksw.org/hasTruthValue");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println(
                    "Error: wrong usage. CSV2FCDatasetConverter <csv-file> <output-file> [fact-checking-dataset-file]");
            return;
        }
        String csvFile = args[0];
        String outputFile = args[1];

        Map<String, String> triple2StmtIri = null;
        String stmtPrefix = "http://example.org/";
        if (args.length > 2) {
            String fcFile = args[2];
            // Get a mapping from triples to their statement IRIs
            triple2StmtIri = mapTriplesToStmts(fcFile);
        }

        // Read CSV file line by line and...
        try (CSVReader reader = new CSVReader(new FileReader(csvFile));
                Writer outSelected = new FileWriter(outputFile)) {
            String[] line = reader.readNext();
            String serializedTriple;
            Node stmtNode;
            StreamRDF stream = StreamRDFLib.writer(outSelected);
            stream.start();
            int count = 0;
            while (line != null) {
                // Ignore header line if there is one
                if (line[1].startsWith("http")) {
                    serializedTriple = line[0] + line[1] + line[2];
                    if ((triple2StmtIri == null) || (triple2StmtIri.containsKey(serializedTriple))) {
                        stmtNode = NodeFactory.createURI(
                                triple2StmtIri != null ? triple2StmtIri.get(serializedTriple) : stmtPrefix + count);
                        // Write triple
                        StreamUtils.writeReifiedStatement(stmtNode, NodeFactory.createURI(line[0]),
                                NodeFactory.createURI(line[1]), NodeFactory.createURI(line[2]), stream);
                        // Add veracity value
                        stream.triple(Triple.create(stmtNode, VERACITY_PROPERTY.asNode(),
                                NodeFactory.createLiteral(line[3], XSDDatatype.XSDdouble)));
                        ++count;
                    } else {
                        LOGGER.error("Couldn't find the triple \"{}\", \"{}\", \"{}\".", line[0], line[1], line[2]);
                    }
                }
                line = reader.readNext();
            }
            stream.finish();
        }

        LOGGER.info("Finished");
    }

    private static Map<String, String> mapTriplesToStmts(String fcFile) {
        final Map<String, String[]> stmt2TripleIris = new HashMap<>();

        // Collect s, p and o IRIs for the statements
        // Create consumers
        StreamRDF objStream = new RDFStreamTripleConsumer(
                t -> putOrAdd(stmt2TripleIris, t.getSubject().getURI(), t.getObject().getURI(), 2));
        StreamRDF predStream = new RDFStreamTripleConsumer(
                t -> putOrAdd(stmt2TripleIris, t.getSubject().getURI(), t.getObject().getURI(), 1));
        StreamRDF subStream = new RDFStreamTripleConsumer(
                t -> putOrAdd(stmt2TripleIris, t.getSubject().getURI(), t.getObject().getURI(), 0));

        // Split the stream based on the predicate
        StreamRDF stream = new RDFStreamTripleFilter(
                new NodeFilterBasedTripleFilter(null, p -> RDF.object.getURI().equals(p.getURI()), null), objStream);
        stream = new RDFStreamTripleFilter(
                new NodeFilterBasedTripleFilter(null, p -> RDF.predicate.getURI().equals(p.getURI()), null), predStream,
                stream);
        stream = new RDFStreamTripleFilter(
                new NodeFilterBasedTripleFilter(null, p -> RDF.subject.getURI().equals(p.getURI()), null), subStream,
                stream);

        // Add monitor at the beginning of the stream
        ProgressMonitor monitorS = new ProgressMonitorOutput("Processed triples", 1000, 10,
                MonitorOutputs.outputToLog(LOGGER));
        stream = new ProgressStreamRDF(stream, monitorS);

        LOGGER.info("Streaming data to get updated statements...");
        // Start reading triples from the input file
        monitorS.start();
        stream.start();
        RDFParser.source(fcFile).lang(Lang.NT).parse(stream);
        monitorS.finish();
        stream.finish();

        // Invert the map
        Map<String, String> triple2StmtIri = new HashMap<>();
        String[] iris;
        String serializedTriples;
        for (String stmtIri : stmt2TripleIris.keySet()) {
            iris = stmt2TripleIris.get(stmtIri);
            serializedTriples = iris[0] + iris[1] + iris[2];
            if (triple2StmtIri.containsKey(serializedTriples)) {
                LOGGER.warn("There are two statements with the same elements! {} and {}",
                        triple2StmtIri.get(serializedTriples), stmtIri);
            } else {
                triple2StmtIri.put(serializedTriples, stmtIri);
            }
        }
        return triple2StmtIri;
    }

    private static void putOrAdd(Map<String, String[]> stmt2TripleIris, String key, String value, int id) {
        String values[];
        if (stmt2TripleIris.containsKey(key)) {
            values = stmt2TripleIris.get(key);
        } else {
            values = new String[3];
            stmt2TripleIris.put(key, values);
        }
        values[id] = value;
    }
}
