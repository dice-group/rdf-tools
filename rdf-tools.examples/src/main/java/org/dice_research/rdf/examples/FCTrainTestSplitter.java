package org.dice_research.rdf.examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
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
 * This class takes a file that can be used to benchmark fact checking
 * approaches as input. Such a file should contain RDF statements (in an reified
 * form) and their veracity value. It splits the file into train and test data
 * according to the following constraints:
 * <ul>
 * <li>The test file should have a size according to the given parameter.</li>
 * <li>Statements in the test file have predicates that have at least one
 * statement in the training file.</li>
 * <li>The test file should be balanced (i.e., it should have the same number of
 * positive and negative examples.</li>
 * </ul>
 * 
 * Note that the training file should be larger than the test file. Otherwise,
 * this class may not work as expected.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class FCTrainTestSplitter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FCTrainTestSplitter.class);

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println(
                    "Error: wrong usage. FCTrainTestSplitter <input-file> <training-file> <test-file> <test-file-size> [seed]");
            return;
        }
        String inputFile = args[0];
        String outputTrainFile = args[1];
        String outputTestFile = args[2];
        int testFileSize = Integer.parseInt(args[3]);
        testFileSize /= 2;
        long seed;
        if (args.length >= 5) {
            seed = Long.parseLong(args[4]);
        } else {
            seed = System.currentTimeMillis();
        }

        String tempTrueFile = File.createTempFile("True", ".nt").getAbsolutePath();
        String tempFalseFile = File.createTempFile("False", ".nt").getAbsolutePath();

        // Split the input file into true and false statements
        Set<String> trueStmts = selectTrueStmts(inputFile);
        splitInputFile(inputFile, tempTrueFile, tempFalseFile, trueStmts);

        String[] sourceFiles = new String[] { tempTrueFile, tempFalseFile };
        for (int i = 0; i < sourceFiles.length; ++i) {
            // 1st iteration: analyze predicates
            Map<String, Set<String>> stmtsByPredicate = new HashMap<>();
            groupStmtsByPredicate(sourceFiles[i], stmtsByPredicate);

            // Analyze predicates
            Set<String> selectedStmts = selectTestStmts(stmtsByPredicate, testFileSize, seed);

            // 2nd iteration: split the input file
            splitInputFile(sourceFiles[i], outputTestFile, outputTrainFile, selectedStmts, i != 0);
        }
        LOGGER.info("Finished");
    }

    public static Set<String> selectTrueStmts(String inputFile) {
        // We collect the IRIs of statements that have a veracity value of 1.0
        Set<String> trueStmts = new HashSet<>();
        StreamRDF stream = new RDFStreamCollector<String>(t -> t.getSubject().getURI(), trueStmts);
        // Hence, we should make sure that we only have triples with veracity values
        final String TRUTH_PREDICATE_IRI = "http://swc2017.aksw.org/hasTruthValue";
        stream = new RDFStreamTripleFilter(
                new NodeFilterBasedTripleFilter(null, p -> TRUTH_PREDICATE_IRI.equals(p.getURI()),
                        o -> o.isLiteral() && "1.0".equals(o.getLiteral().getLexicalForm())),
                stream);

        // Add monitor at the beginning of the stream
        ProgressMonitor monitorS = ProgressMonitor.create(LOGGER, "Processed triples", 1000, 10);
        stream = new ProgressStreamRDF(stream, monitorS);

        LOGGER.info("Streaming data to select true triples...");
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
