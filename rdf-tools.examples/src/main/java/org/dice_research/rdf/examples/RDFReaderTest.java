package org.dice_research.rdf.examples;

import java.io.File;
import java.io.IOException;

import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple class that takes a list of input RDF files, reads them in the given
 * order and streams their content to a given output file. If one of the given
 * files is a directory, the program will go through the directory recursively.
 * Note that the input files can be compressed using gzip or bzip2. The output
 * file will always be a compressed N-Triples file.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class RDFReaderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDFReaderTest.class);

    static {
        JenaSystem.init();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            LOGGER.error("Wrong usage! RDFReaderTest <input-file> [language]");
            return;
        }
        File files = new File(args[0]);
        runTest(files, args.length > 1 ? args[1] : null);
    }

    private static void runTest(File file, String languageHint) throws IOException {
        ProgressMonitor monitorS = new ProgressMonitorOutput("Processed triples", 1000, 10,
                MonitorOutputs.outputToLog(LOGGER));
        StreamRDF stream = new ProgressStreamRDF(StreamRDFLib.sinkNull(), monitorS);
        monitorS.start();
        stream.start();
        RDFParser.source(file.toURI().toURL().toString()).parse(stream);
        monitorS.finish();
        stream.finish();
        LOGGER.info("Finished.");
    }

}
