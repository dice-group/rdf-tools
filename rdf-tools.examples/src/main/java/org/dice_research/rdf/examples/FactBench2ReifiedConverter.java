package org.dice_research.rdf.examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.dice_research.rdf.stream.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactBench2ReifiedConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactBench2ReifiedConverter.class);

    private static final Resource VERACITY_PROPERTY = ResourceFactory
            .createResource("http://swc2017.aksw.org/hasTruthValue");
    private static final Node TRUE_VALUE = NodeFactory.createLiteral("1.0", XSDDatatype.XSDdouble);
    private static final Node FALSE_VALUE = NodeFactory.createLiteral("0.0", XSDDatatype.XSDdouble);

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println(
                    "Error: wrong usage. FactBench2ReifiedConverter <true-directory> <false-directory> <dataset-namespace> <output-file>");
            return;
        }
        File trueDirectory = new File(args[0]);
        File falseDirectory = new File(args[1]);
        String namespace = args[2];
        if (!namespace.endsWith("/") && !namespace.endsWith("#")) {
            namespace += "/";
        }
        String outputFile = args[3];

        // Recursively go through the directory and parse the files
        int count = 0;
        try (Writer writer = new FileWriter(outputFile)) {
            StreamRDF stream = StreamRDFLib.writer(writer);
            stream.start();
            count = readDirectory(trueDirectory, namespace, TRUE_VALUE, stream);
            count += readDirectory(falseDirectory, namespace, FALSE_VALUE, stream);
            stream.finish();
        }
        LOGGER.info("Finished. Processed {} files.", count);
    }

    private static int readDirectory(File inputDirectory, String namespace, Node veracityValue, StreamRDF stream) {
        LOGGER.info("Processing files in {} ...", inputDirectory);
        int count = 0;
        Resource event;
        Resource[] triple;
        for (File inputFile : inputDirectory.listFiles()) {
            if (inputFile.isDirectory()) {
                // read directory
                count += readDirectory(inputFile, namespace, veracityValue, stream);
            } else {
                // read file
                Model model = ModelFactory.createDefaultModel();
                try {
                    model.read(inputFile.toURI().toURL().toString());
                    event = selectEvent(model);
                    if (event != null) {
                        triple = selectTriple(event, model);
                        if (triple != null) {
                            writeReifiedStmt(triple, namespace, veracityValue, inputFile, stream);
                            ++count;
                        } else {
                            LOGGER.warn(
                                    "Couldn't find triple for intermediate node {} in {}. The file will be ignored.",
                                    event.toString(), inputFile);
                        }
                    } else {
                        LOGGER.warn("Couldn't find intermediate node in {}. The file will be ignored.", inputFile);
                    }
                    model.close();
                } catch (Exception e) {
                    LOGGER.error("Exception while reading file " + inputFile.toString() + ". The file will be ignored.",
                            e);
                }
            }
        }
        return count;
    }

    private static Resource selectEvent(Model model) {
        ResIterator iter = model.listSubjects();
        Resource r;
        while (iter.hasNext()) {
            r = iter.next();
            if (r.isURIResource() && r.getURI().contains("__")) {
                return r;
            }
        }
        return null;
    }

    private static Resource[] selectTriple(Resource event, Model model) {
        Resource[] triple = new Resource[3];
        StmtIterator iterator;
        iterator = model.listStatements(null, null, event);
        if (iterator.hasNext()) {
            triple[0] = iterator.next().getSubject();
        } else {
            return null;
        }
        iterator = model.listStatements(event, null, (RDFNode) null);
        Statement stmt;
        while (iterator.hasNext()) {
            stmt = iterator.next();
            if (stmt.getObject().isURIResource()) {
                triple[1] = stmt.getPredicate();
                triple[2] = stmt.getObject().asResource();
                return triple;
            }
        }
        return null;
    }

    private static void writeReifiedStmt(Resource[] triple, String namespace, Node veracityValue, File inputFile,
            StreamRDF stream) {
        Node stmt = NodeFactory.createURI(namespace + inputFile.getName());
        // Write triple
        StreamUtils.writeReifiedStatement(stmt, triple[0].asNode(), triple[1].asNode(), triple[2].asNode(), stream);
        // Add veracity value
        stream.triple(Triple.create(stmt, VERACITY_PROPERTY.asNode(), veracityValue));
    }
}
