package org.dice_research.rdf.examples;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.CharSpace;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.rdf.stream.collect.RDFStreamCollector;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example streams a given RDF file, collects the properties that are used,
 * detects whether they are used as instance of {@code owl:DatatypeProperty} or
 * {@code owl:ObjectProperty}, and writes the resulting type triples for the
 * properties into the given output file.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class PropertyTypeCollector {

    static {
        JenaSystem.init();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyTypeCollector.class);

    public static StreamRDF createStream(Set<String> objectProperties, Set<String> datatypeProperties) {
        StreamRDF streamO = new RDFStreamCollector<String>(t -> t.getPredicate().getURI(), objectProperties);
        StreamRDF streamD = new RDFStreamCollector<String>(t -> t.getPredicate().getURI(), datatypeProperties);
        StreamRDF stream = new RDFStreamTripleFilter(t -> t.getObject().isLiteral(), streamD, streamO);
        return stream;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Error: wrong usage. PropertyTypeCollector <input-file> <output-file>");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];

        Set<String> datatypeProperties = new HashSet<>();
        Set<String> objectProperties = new HashSet<>();

        // Create stream starting from the end!
        StreamRDF stream = createStream(objectProperties, datatypeProperties);

        // Add monitor at the beginning of the stream
        ProgressMonitor monitorS = new ProgressMonitorOutput("Processed triples", 100000, 10,
                MonitorOutputs.outputToLog(LOGGER));
        stream = new ProgressStreamRDF(stream, monitorS);

        LOGGER.info("Streaming data...");
        // Start reading triples from the input file
        monitorS.start();
        stream.start();
        RDFParser.source(inputFile).lang(Lang.NT).parse(stream);
        monitorS.finish();
        stream.finish();

        LOGGER.info("Writing Properties");
        try (OutputStream fout = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            final StreamRDF outStream = StreamRDFLib.writer(fout, CharSpace.UTF8);
            outStream.start();
            for (String iri : datatypeProperties) {
                outStream.triple(
                        Triple.create(NodeFactory.createURI(iri), RDF.type.asNode(), OWL.DatatypeProperty.asNode()));
            }
            for (String iri : objectProperties) {
                outStream.triple(
                        Triple.create(NodeFactory.createURI(iri), RDF.type.asNode(), OWL.ObjectProperty.asNode()));
            }
            outStream.finish();
        } catch (IOException e) {
            LOGGER.error("Writer catched IOException. Terminating.", e);
        }
        LOGGER.info("Writer terminated.");

        LOGGER.info("Finished");
    }
}
