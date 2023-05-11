package org.dice_research.rdf.examples;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWrapper;
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
public class RDFCat {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDFCat.class);

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            LOGGER.error("Wrong usage! RDFCat <output-file> <input-file> [<input-file>...]");
            return;
        }
        File[] files = new File[args.length - 1];
        for (int i = 1; i < args.length; ++i) {
            files[i - 1] = new File(args[i]);
        }
//        runSingleThread(files, args[0]);
        runMultiThread(files, args[0]);
    }

//    private static void runSingleThread(File[] files, String outputFile) throws IOException {
//        try (OutputStream fout = new BZip2CompressorOutputStream(
//                new BufferedOutputStream(new FileOutputStream(outputFile)))) {
//            StreamRDF outStream = StreamRDFLib.writer(fout);
//            outStream.start();
//            for (File file : files) {
//                addFile2Stream(file, outStream);
//            }
//            outStream.finish();
//        }
//        LOGGER.info("Finished.");
//    }

    private static void addFile2Stream(File file, StreamRDF outStream) throws IOException {
        if (file.isDirectory()) {
            // read directory
            for (File inputFile : file.listFiles()) {
                addFile2Stream(inputFile, outStream);
            }
        } else {
            LOGGER.info("Adding file {} ...", file.toString());
            try (InputStream in = streamFile(file)) {
                RDFDataMgr.parse(outStream, in, Lang.TTL);
            }
        }
    }

    private static InputStream streamFile(File file) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        if (file.getName().endsWith(".bz2")) {
            in = new BZip2CompressorInputStream(in);
        } else if (file.getName().endsWith(".gz")) {
            in = new GzipCompressorInputStream(in);
        }
        return in;
    }

    private static void runMultiThread(File[] files, String outputFile) throws IOException {
        final PipedRDFIterator<Triple> iterator = new PipedRDFIterator<>();
        StreamRDF pipedStream = new PipedTriplesStream(iterator);
        StreamRDF multiFileStream = new StreamRDFWrapper(pipedStream) {
            @Override
            public void finish() {
                // Do not forward finish messages.
            }
        };
        // Create compressor output thread
        Thread tout = new Thread(new Runnable() {
            @Override
            public void run() {
                try (OutputStream fout = new BZip2CompressorOutputStream(
                        new BufferedOutputStream(new FileOutputStream(outputFile)))) {
                    final StreamRDF outStream = StreamRDFLib.writer(fout);
                    outStream.start();
                    iterator.forEachRemaining(t -> outStream.triple(t));
                    outStream.finish();
                } catch (IOException e) {
                    LOGGER.error("Writer catched IOException. Terminating.", e);
                }
                LOGGER.info("Writer terminated.");
            }
        });
        tout.start();

        for (File file : files) {
            addFile2Stream(file, multiFileStream);
        }
        pipedStream.finish(); // Tell the piped stream that there won't be any input
        try {
            if (tout != null) {
                tout.join();
            }
        } catch (InterruptedException e) {
            LOGGER.error("Exception while merging threads.", e);
        }
        LOGGER.info("Finished.");
    }
}
