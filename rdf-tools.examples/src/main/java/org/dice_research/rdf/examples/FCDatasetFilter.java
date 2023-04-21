package org.dice_research.rdf.examples;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.ProgressStreamRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.dice_research.rdf.stream.collect.RDFStreamCollector;
import org.dice_research.rdf.stream.filter.NodeFilterBasedTripleFilter;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes two RDF files that are used for fact checking. One file is
 * used to generate a list of reified RDF statements (i.e., the statement IRIs
 * are gathered). Then, only RDF statements with the same IRI are taken from the
 * second file. This class can be used to separate a large, updated file of RDF
 * statements into its original parts (based on the old versions of these part
 * files).
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class FCDatasetFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FCDatasetFilter.class);

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println(
                    "Error: wrong usage. FCTrainTestSplitter <correct-stmt-file> <large-stmt-file> <filtered-file>");
            return;
        }
        String correctFile = args[0];
        String largeFile = args[1];
        String outputFile = args[2];

        // Get statement IRIs
        Set<String> selectedStmts = selectStmtsIRIs(correctFile);

        try (Writer outSelected = new FileWriter(outputFile)) {
            // Create stream starting from the end!
            StreamRDF selectedStream = StreamRDFLib.writer(outSelected);

            // Split stream based on whether a statement (i.e., the subject) has been
            // selected or not
            StreamRDF stream = new RDFStreamTripleFilter(
                    new NodeFilterBasedTripleFilter(s -> s.isURI() && selectedStmts.contains(s.getURI()), null, null),
                    selectedStream);

            // Add monitor at the beginning of the stream
            ProgressMonitor monitorS = ProgressMonitor.create(LOGGER, "Processed triples", 1000, 10);
            stream = new ProgressStreamRDF(stream, monitorS);

            LOGGER.info("Streaming data to get updated statements...");
            // Start reading triples from the input file
            monitorS.start();
            stream.start();
            RDFDataMgr.parse(stream, largeFile, Lang.NT);
            monitorS.finish();
            stream.finish();
        }

        LOGGER.info("Finished");
    }

    public static Set<String> selectStmtsIRIs(String inputFile) {
        // We collect the IRIs of statements that have a veracity value of 1.0
        Set<String> trueStmts = new HashSet<>();
        StreamRDF stream = new RDFStreamCollector<String>(t -> t.getSubject().getURI(), trueStmts);

        // Add monitor at the beginning of the stream
        ProgressMonitor monitorS = ProgressMonitor.create(LOGGER, "Processed triples", 1000, 10);
        stream = new ProgressStreamRDF(stream, monitorS);

        LOGGER.info("Streaming data to select statement IRIs...");
        // Start reading triples from the input file
        monitorS.start();
        stream.start();
        RDFDataMgr.parse(stream, inputFile, Lang.NT);
        monitorS.finish();
        stream.finish();

        return trueStmts;
    }

}
