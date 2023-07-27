package org.dice_research.rdf.examples;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDF2;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.dice_research.rdf.stream.collect.RDFStreamCollector;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class collects numerical properties, i.e., properties that have only
 * numerical literals as objects.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class NumericalPropertyCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumericalPropertyCollector.class);

    protected Set<String> collectedProperties = new HashSet<String>();

    public void collectFromDirectory(File input) throws IOException {
        if (input.isDirectory()) {
            for (File file : input.listFiles()) {
                collectFromDirectory(file);
            }
        } else if (input.isFile()) {
            collectFromFile(input);
        } else {
            LOGGER.error("{} is neither a directory nor a file. It will be ignored.", input);
        }
    }

    public void collectFromFile(File input) throws IOException {
        String fileName = input.getName();
        if (fileName.contains(".nt") || fileName.contains(".ttl")) {
            try (InputStream is = openStream(input)) {
                if (is == null) {
                    return;
                }
                LOGGER.info("Streaming {}...", fileName);
                collectFromStream(is);
            }
        } else {
            LOGGER.info("Can't read {} since it is neither nt nor turtle.", fileName);
        }
    }

    private InputStream openStream(File input) throws IOException {
        String fileName = input.getName();
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(input));
            if (fileName.endsWith(".bz2")) {
                is = new BZip2CompressorInputStream(is);
            }
            return is;
        } catch (IOException e) {
            // just make sure that we close the stream, in case it is already open
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e2) {
                    // Nothing to do
                }
            }
            throw e;
        }
    }

    private void collectFromStream(InputStream is) {
        Set<String> properties = new HashSet<String>();
        Set<String> nonNumericProperties = new HashSet<String>();
        // Collect all properties that have not (only) non-numerical objects
        StreamRDF nonNumericStream = new RDFStreamCollector<String>(t -> t.getPredicate().getURI(),
                nonNumericProperties);
        // Get a stream of all triples that have either no literal as object or a
        // literal that is not numeric
        nonNumericStream = new RDFStreamTripleFilter(t -> ((!t.getObject().isLiteral())
                || (t.getObject().isLiteral() && !t.getObject().getLiteral().getLexicalForm().matches("[0-9,.]+"))),
                nonNumericStream);
        // Get another stream that simply collects all properties
        StreamRDF propStream = new RDFStreamCollector<String>(t -> t.getPredicate().getURI(), properties);
        // Split the stream
        StreamRDF stream = new StreamRDF2(nonNumericStream, propStream);

        // Add monitor at the beginning of the stream
        ProgressMonitor monitorS = new ProgressMonitorOutput("Processed triples", 10000, 10,
                MonitorOutputs.outputToLog(LOGGER));
        stream = new ProgressStreamRDF(stream, monitorS);

        // Start reading triples from the input file
        monitorS.start();
        stream.start();
        RDFParser.source(is).lang(Lang.TURTLE).parse(stream);
        monitorS.finish();
        stream.finish();

        LOGGER.info("found {}/{} that are non numerical", nonNumericProperties.size(), properties.size());
        properties.removeAll(nonNumericProperties);
        collectedProperties.addAll(properties);
    }

    public Set<String> getCollectedProperties() {
        return collectedProperties;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Error: wrong usage. NumericalPropertyCollector <input-directory> <output-file>");
            return;
        }
        File input = new File(args[0]);
        File output = new File(args[1]);

        NumericalPropertyCollector collector = new NumericalPropertyCollector();
        collector.collectFromDirectory(input);
        LOGGER.info("Writing output.");
        Set<String> properties = collector.getCollectedProperties();
        FileUtils.writeLines(output, properties);
        LOGGER.info("Finished.");
    }
}
