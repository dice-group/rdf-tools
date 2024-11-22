package org.dice_research.rdf.examples;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.dice_research.rdf.stream.filter.NodeFilterBasedTripleFilter;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.dice_research.rdf.stream.filter.node.StringBasedNamespaceNodeFilter;
import org.dice_research.rdf.stream.map.RDFStreamTripleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tries to identify triples that are in the given RDF file but not in the given
 * SPARQL endpoint. These new triples are written to the given output file.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class NewTriplesSearch implements AutoCloseable, Predicate<Triple> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataPreprocessing.class);
    private final String FAULTY_PREFIX = "https://www.wikidata.org/wiki/";
    private final String ENTITY_NAMESPACE = "http://www.wikidata.org/entity/";
    private final String PROPERTY_NAMESPACE = "http://www.wikidata.org/prop/direct/";

    static {
        JenaSystem.init();
    }

    protected QueryExecutionFactory queryExecFactory;

    public NewTriplesSearch(QueryExecutionFactory queryExecFactory) {
        super();
        this.queryExecFactory = queryExecFactory;
    }

    public void run(String inputFile, String outputFile) throws Exception {
        try (OutputStream out1 = openStream(outputFile)) {
            // Create stream starting from the end!
            StreamRDF stream = StreamRDFLib.writer(out1);
            ProgressMonitor monitor1 = new ProgressMonitorOutput("Added triples", 1000, 10,
                    MonitorOutputs.outputToLog(LOGGER));
            stream = new ProgressStreamRDF(stream, monitor1);

            // Check whether the triple is already known
            StreamRDF checkStream = new RDFStreamTripleFilter(this, stream);

            // Separate triples that should be checked from triples that can be simply
            // written
            stream = new RDFStreamTripleFilter(
                    new NodeFilterBasedTripleFilter(new StringBasedNamespaceNodeFilter(ENTITY_NAMESPACE),
                            new StringBasedNamespaceNodeFilter(PROPERTY_NAMESPACE), null),
                    checkStream, stream);

            // Fix Wikidata IRIs with faulty prefix
            stream = new RDFStreamTripleMapper((t) -> {
                Node s = t.getSubject();
                boolean changed = false;
                if (s.isURI() && s.getURI().startsWith(FAULTY_PREFIX)) {
                    s = NodeFactory.createURI(ENTITY_NAMESPACE + s.getURI().substring(FAULTY_PREFIX.length()));
                    changed = true;
                }
                Node p = t.getPredicate();
                if (p.isURI() && p.getURI().startsWith(FAULTY_PREFIX)) {
                    p = NodeFactory.createURI(PROPERTY_NAMESPACE + p.getURI().substring(FAULTY_PREFIX.length()));
                    changed = true;
                }
                Node o = t.getObject();
                if (o.isURI() && o.getURI().startsWith(FAULTY_PREFIX)) {
                    o = NodeFactory.createURI(ENTITY_NAMESPACE + o.getURI().substring(FAULTY_PREFIX.length()));
                    changed = true;
                }
                if (changed) {
                    return Triple.create(s, p, o);
                } else {
                    return t;
                }
            }, stream);
//
//            // Only use triples which have an object of the wd namespace and a
//            // property of the wdt namespace.
//            stream = new RDFStreamTripleFilter(new NodeFilterBasedTripleFilter(null,
//                    new StringBasedNamespaceNodeFilter(ACCEPTED_PROPERTY_NAMESPACES),
//                    new StringBasedNamespaceNodeFilter(ACCEPTED_ENTITY_NAMESPACES)), stream);
//
//            // Only use triples which define the English RDFS label.
//            StreamRDF labelStream = new RDFStreamTripleFilter(
//                    new NodeFilterBasedTripleFilter(null, n -> RDFS_LABEL_NODE.equals(n),
//                            n -> n.isLiteral() && ACCEPTED_LANGUAGE.equals(n.getLiteralLanguage())),
//                    outStream);
//
//            // Distinguish between IRI objects and other objects
//            stream = new RDFStreamTripleFilter(
//                    new NodeFilterBasedTripleFilter(new StringBasedNamespaceNodeFilter(ACCEPTED_ENTITY_NAMESPACES),
//                            null, n -> n.isURI()),
//                    stream, labelStream);
//
//            // Only use triples which have a subject of the wd namespace
//            stream = new RDFStreamTripleFilter(new NodeFilterBasedTripleFilter(
//                    new StringBasedNamespaceNodeFilter(ACCEPTED_ENTITY_NAMESPACES), null, null), stream);

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

    protected OutputStream openStream(String outputFile) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outputFile)));
        if (outputFile.endsWith(".gz")) {
            out = new GZIPOutputStream(out);
        }
        return out;
    }

    @Override
    public boolean test(Triple t) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("ASK { ");
        addNode(t.getSubject(), queryBuilder);
        queryBuilder.append(' ');
        addNode(t.getSubject(), queryBuilder);
        queryBuilder.append(' ');
        addNode(t.getSubject(), queryBuilder);
        queryBuilder.append(" . }");
        return !queryExecFactory.createQueryExecution(queryBuilder.toString()).execAsk();
    }

    protected void addNode(Node node, StringBuilder queryBuilder) {
        if (node.isURI()) {
            queryBuilder.append('<');
            queryBuilder.append(node.getURI());
            queryBuilder.append('>');
            return;
        }
        if (node.isLiteral()) {
            LiteralLabel literal = node.getLiteral();
            queryBuilder.append('"');
            queryBuilder.append(literal.getLexicalForm());
            queryBuilder.append('"');
            String lang = literal.language();
            if ((lang != null) && (!lang.isEmpty())) {
                queryBuilder.append('@');
                queryBuilder.append(lang);
            } else if (literal.getDatatypeURI() != null) {
                queryBuilder.append("^^<");
                queryBuilder.append(literal.getDatatypeURI());
                queryBuilder.append('>');
            }
            return;
        }
        if (node.isBlank()) {
            queryBuilder.append("_:");
            queryBuilder.append(node.getBlankNodeLabel());
            return;
        }
    }

    @Override
    public void close() throws Exception {
        queryExecFactory.close();
    }

    public static NewTriplesSearch create(String endpoint) {
        HttpClient client = HttpClient.newHttpClient();
        return new NewTriplesSearch(new QueryExecutionFactoryDelay(
                new QueryExecutionFactoryHttp(endpoint, new DatasetDescription(), client), 500));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Error: wrong usage. NewTriplesSearch <input-file> <endpoint> <output-file>");
            return;
        }
        String inputFile = args[0];
        String endpoint = args[1];
        String outputFile = args[2];

        NewTriplesSearch search = NewTriplesSearch.create(endpoint);
        search.run(inputFile, outputFile);
    }
}
