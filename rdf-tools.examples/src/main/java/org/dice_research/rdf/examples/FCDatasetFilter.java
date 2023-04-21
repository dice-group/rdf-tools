package org.dice_research.rdf.examples;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.jena.atlas.lib.ProgressMonitor;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.ProgressStreamRDF;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.rdf.stream.collect.RDFStreamCollector;
import org.dice_research.rdf.stream.collect.RDFStreamGroupByCollector;
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

    public static void groupStmtsByPredicate(String inputFile, Map<String, Set<String>> stmtsByPredicate) {
        // We collect the IRI of the subject (i.e., the statement IRI) and group these
        // IRIs by objects (i.e., the property of the statement) assuming that we only
        // see triples with rdf:predicate as predicate.
        StreamRDF stream = new RDFStreamGroupByCollector<>(t -> t.getObject().getURI(), t -> t.getSubject().getURI(),
                HashSet::new, stmtsByPredicate);
        // Hence, we should make sure that we only have rdf:predicate triples
        final String RDF_PREDICATE_IRI = RDF.predicate.getURI();
        stream = new RDFStreamTripleFilter(t -> RDF_PREDICATE_IRI.equals(t.getPredicate().getURI()), stream);

        // Add monitor at the beginning of the stream
        ProgressMonitor monitorS = ProgressMonitor.create(LOGGER, "Processed triples", 1000, 10);
        stream = new ProgressStreamRDF(stream, monitorS);

        LOGGER.info("Streaming data to analyze predicates...");
        // Start reading triples from the input file
        monitorS.start();
        stream.start();
        RDFDataMgr.parse(stream, inputFile, Lang.NT);
        monitorS.finish();
        stream.finish();
    }

    public static Set<String> selectTestStmts(Map<String, Set<String>> stmtsByPredicate, int testFileSize, long seed) {
        Set<String> excludedPredicates = new HashSet<String>();
        Set<String> stmts;
        int predicateCount = 0;
        int statementCount = 0;
        for (String predicate : stmtsByPredicate.keySet()) {
            stmts = stmtsByPredicate.get(predicate);
            // Exclude predicates that occur only once
            if (stmts.size() <= 1) {
                excludedPredicates.add(predicate);
            } else {
                ++predicateCount;
                statementCount += stmts.size();
            }
        }
        // calculate the number of training examples that have to be selected
        int maximumTestSize = statementCount - predicateCount;
        if (maximumTestSize < testFileSize) {
            throw new IllegalArgumentException(
                    "The given test file size is too high. The maximum test file size is " + maximumTestSize + ".");
        }
        // Select test statements randomly
        Set<String> selectedStmts = new HashSet<String>();
        boolean first;
        Random random = new Random(seed);
        for (String predicate : stmtsByPredicate.keySet()) {
            if (!excludedPredicates.contains(predicate)) {
                stmts = stmtsByPredicate.get(predicate);
                first = true;
                for (String stmt : stmts) {
                    // the first statement is always a training statement
                    if (first) {
                        first = false;
                    } else {
                        // Randomly select whether this statement is a training or test statement
                        if (random.nextInt(maximumTestSize) < testFileSize) {
                            // Add it to the set of test statements
                            selectedStmts.add(stmt);
                            --testFileSize;
                        }
                        --maximumTestSize;
                    }
                }
            }
        }
        return selectedStmts;
    }

    public static void splitInputFile(String inputFile, String selectedFile, String otherFile,
            Set<String> selectedStmts) throws IOException {
        splitInputFile(inputFile, selectedFile, otherFile, selectedStmts, false);
    }

    public static void splitInputFile(String inputFile, String selectedFile, String otherFile,
            Set<String> selectedStmts, boolean append) throws IOException {

        try (Writer outSelected = new FileWriter(selectedFile, append);
                Writer outOther = new FileWriter(otherFile, append)) {
            // Create stream starting from the end!
            StreamRDF selectedStream = StreamRDFLib.writer(outSelected);
            StreamRDF otherStream = StreamRDFLib.writer(outOther);

            // Split stream based on whether a statement (i.e., the subject) has been
            // selected or not
            StreamRDF stream = new RDFStreamTripleFilter(
                    new NodeFilterBasedTripleFilter(s -> s.isURI() && selectedStmts.contains(s.getURI()), null, null),
                    selectedStream, otherStream);

            // Add monitor at the beginning of the stream
            ProgressMonitor monitorS = ProgressMonitor.create(LOGGER, "Processed triples", 1000, 10);
            stream = new ProgressStreamRDF(stream, monitorS);

            LOGGER.info("Streaming data to split into two files...");
            // Start reading triples from the input file
            monitorS.start();
            stream.start();
            RDFDataMgr.parse(stream, inputFile, Lang.NT);
            monitorS.finish();
            stream.finish();
        }
    }
}
