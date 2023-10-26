package org.dice_research.rdf.examples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.rdf.stream.filter.NodeFilterBasedTripleFilter;
import org.dice_research.rdf.stream.filter.PropertyBasedTripleFilter;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.dice_research.rdf.stream.filter.node.StringBasedNamespaceNodeFilter;
import org.dice_research.rdf.stream.map.RDFStreamTripleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple application that preprocesses a Wikidata dump by 1) removing all
 * triples except "truthy" triples that connect two Wikidata entities and 2)
 * replacing wdt:P31 triples with rdf:type triples.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class WikidataPreprocessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataPreprocessing.class);

    private static final String WDT_P31_IRI = "http://www.wikidata.org/prop/direct/P31";
    private static final Node RDF_TYPE_NODE = RDF.type.asNode();
    private static final String[] ACCEPTED_ENTITY_NAMESPACES = { "http://www.wikidata.org/entity/" };
    private static final String[] ACCEPTED_PROPERTY_NAMESPACES = { "http://www.wikidata.org/prop/direct/" };

    public void run(String inputFile, String outputFile) throws FileNotFoundException, IOException {
        try (Writer out1 = new FileWriter(outputFile)) {
            // Create stream starting from the end!
            StreamRDF outStream = StreamRDFLib.writer(out1);
            ProgressMonitor monitor1 = new ProgressMonitorOutput("Added triples", 100000, 10,
                    MonitorOutputs.outputToLog(LOGGER));
            outStream = new ProgressStreamRDF(outStream, monitor1);

            // From the remaining triples, take those that have a dbo property which is not
            // on the blacklist
            StreamRDF stream = new RDFStreamTripleMapper(
                    (t) -> Triple.create(t.getSubject(), RDF_TYPE_NODE, t.getObject()), outStream);

            // Write all triples except wdt:P31 triples to the output
            stream = new RDFStreamTripleFilter(new PropertyBasedTripleFilter(WDT_P31_IRI), stream, outStream);

            // Only use triples which have a subject and object of the wd namespace and a
            // property of the wdt namespace.
            stream = new RDFStreamTripleFilter(
                    new NodeFilterBasedTripleFilter(new StringBasedNamespaceNodeFilter(ACCEPTED_ENTITY_NAMESPACES),
                            new StringBasedNamespaceNodeFilter(ACCEPTED_PROPERTY_NAMESPACES),
                            new StringBasedNamespaceNodeFilter(ACCEPTED_ENTITY_NAMESPACES)),
                    stream);

            monitor1.start();
            LOGGER.info("Streaming file {}.", inputFile);
            ProgressMonitor monitorS = new ProgressMonitorOutput("Processed triples", 100000, 10,
                    MonitorOutputs.outputToLog(LOGGER));
            StreamRDF fileStream = new ProgressStreamRDF(stream, monitorS);
            monitorS.start();
            // If we have a bz2 file
            if (inputFile.endsWith("bz2")) {
                try (BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(inputFile))) {
                    RDFParser.source(bzIn).lang(Lang.NT).parse(fileStream);
                }
            } else if (inputFile.endsWith("gz")) {
                try (GZIPInputStream gzIn = new GZIPInputStream(new FileInputStream(inputFile))) {
                    RDFParser.source(gzIn).lang(Lang.NT).parse(fileStream);
                }
            } else {
                RDFParser.source(inputFile).lang(Lang.NT).parse(fileStream);
            }
            monitor1.finish();
            stream.finish();
            LOGGER.info("Finished");
        }
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if (args.length < 2) {
            System.err.println("Error: wrong usage. WikidataPreprocessing <input-file> <output-file>");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];

        WikidataPreprocessing preprocessing = new WikidataPreprocessing();
        preprocessing.run(inputFile, outputFile);
    }
}
