package org.dice_research.rdf.examples;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.rdf.stream.filter.NodeFilterBasedTripleFilter;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.dice_research.rdf.stream.filter.node.StringBasedNamespaceNodeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This example takes a list of DBpedia files which are streamed, filtered and
 * written to a single output file. Each triple has to fulfill the following
 * requirements:
 * <ul>
 * <li>Its subject should be an element of the dbr or dbo namespace.</li>
 * <li>Its property should be from one of the following namespaces: dbo, rdf,
 * rdfs.</li>
 * <li>Its object should not be a literal.</li>
 * <li>Its property should not be on the blacklist of this class.</li>
 * </ul>
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class DBOPreprocessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBOPreprocessor.class);

    private static final Set<String> PROPERTY_BLACKLIST = new HashSet<String>(Arrays.asList(
            "http://dbpedia.org/ontology/wikiPageExternalLink", "http://dbpedia.org/ontology/dbo:wikiPageWikiLink"));

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            LOGGER.error("Wrong usage: DBOPreprocessor <output file> <input file> ...");
        }

        String outputFile = args[0];

        try (Writer out1 = new FileWriter(outputFile)) {
            // Create stream starting from the end!
            StreamRDF outStream = StreamRDFLib.writer(out1);
            ProgressMonitor monitor1 = new ProgressMonitorOutput("Added triples", 100000, 10,
                    MonitorOutputs.outputToLog(LOGGER));
            outStream = new ProgressStreamRDF(outStream, monitor1);

            // From the remaining triples, take thos that have a dbo property which is not
            // on the blacklist
            StreamRDF stream = new RDFStreamTripleFilter(
                    new NodeFilterBasedTripleFilter(null, p -> p.getURI().startsWith("http://dbpedia.org/ontology/")
                            && !PROPERTY_BLACKLIST.contains(p.getURI()), null),
                    outStream);

            // Write RDF and RDFS triples to the output; forward all other triples to the
            // next filter
            stream = new RDFStreamTripleFilter(new NodeFilterBasedTripleFilter(null,
                    new StringBasedNamespaceNodeFilter(RDF.getURI(), RDFS.getURI()), null), outStream, stream);

            // Only use triples which have a subject of the dbo or dbr namespace. Make sure that the object is not a literal.
            stream = new RDFStreamTripleFilter(new NodeFilterBasedTripleFilter(
                    new StringBasedNamespaceNodeFilter("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/"),
                    null, o -> !o.isLiteral()), stream);

            monitor1.start();
            // For each file, stream the file
            for (int i = 1; i < args.length; ++i) {
                String inputFile = args[i];
                LOGGER.info("Streaming file {}.", inputFile);
                ProgressMonitor monitorS = new ProgressMonitorOutput("Processed triples", 100000, 10,
                        MonitorOutputs.outputToLog(LOGGER));
                StreamRDF fileStream = new ProgressStreamRDF(stream, monitorS);
                monitorS.start();
                // If we have a bz2 file
                if(inputFile.endsWith("bz2")) {
                    try(BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(inputFile))) {
                        RDFParser.source(bzIn).lang(Lang.NT).parse(fileStream);
                    }
                } else {
                    RDFParser.source(inputFile).lang(Lang.NT).parse(fileStream);
                }
            }
            monitor1.finish();
            stream.finish();
            LOGGER.info("Finished");
        }
    }
}
