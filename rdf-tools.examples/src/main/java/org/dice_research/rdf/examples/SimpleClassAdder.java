package org.dice_research.rdf.examples;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDF2;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorBasic;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.rdf.stream.filter.NodeFilterBasedTripleFilter;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.dice_research.rdf.stream.filter.node.EqualityNodeFilter;
import org.dice_research.rdf.stream.filter.node.StringBasedNamespaceNodeFilter;
import org.dice_research.rdf.stream.map.RDFStreamTripleFlatMapper;
import org.dice_research.serial.maps.ComplexHashMapDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class SimpleClassAdder implements Function<Triple, Stream<Triple>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClassAdder.class);

    protected Map<String, ? extends Collection<String>> classHierarchy;

    public SimpleClassAdder(Map<String, ? extends Collection<String>> classHierarchy) {
        this.classHierarchy = classHierarchy;
    }

    @Override
    public Stream<Triple> apply(Triple t) {
        Node object = t.getObject();
        if (!object.isURI()) {
            return Stream.empty();
        }
        if (classHierarchy.containsKey(object.getURI())) {
            return classHierarchy.get(object.getURI()).stream().map(NodeFactory::createURI)
                    .map(classNode -> new Triple(t.getSubject(), t.getPredicate(), classNode));
        } else {
            return Stream.empty();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            LOGGER.error(
                    "Wrong usage! GlistenClassAdder <input-file> <class-map-file> <domain-range-map-file> <output-file>");
            return;
        }
        String inputFile = args[0];
        String classMapFile = args[1];
        String drInfoFile = args[2];
        String outputFile = args[3];
        Map<String, ArrayList<String>> classHierarchy = readClassHierarchy(new File(classMapFile));
        @SuppressWarnings("rawtypes")
        HashMap[] drInformation = DRBasedClassAdder.readDRInformation(new File(drInfoFile));

        try (Writer out1 = new FileWriter(outputFile)) {
            // Create stream starting from the end!
            StreamRDF outStream = StreamRDFLib.writer(out1);
            ProgressMonitor monitor1 = new ProgressMonitorOutput("Added triples", 100000, 10,
                    MonitorOutputs.outputToLog(LOGGER));
            outStream = new ProgressStreamRDF(outStream, monitor1);

            // First stream, extend existing or newly created rdf:type statements with super
            // classes
            // Filter type triples again just in case we added owl:Thing or similar
            StreamRDF typeStream = new RDFStreamTripleFilter(
                    new NodeFilterBasedTripleFilter(null, new EqualityNodeFilter(RDF.type.asNode()),
                            new StringBasedNamespaceNodeFilter("http://dbpedia.org/ontology/")),
                    outStream);

            // Map the incoming triples to newly generated triples
            typeStream = new RDFStreamTripleFlatMapper(new SimpleClassAdder(classHierarchy), typeStream);

            // Second stream: we add domain and range information; we write it to the type
            // stream so that this stream can add more classes if necessary
            @SuppressWarnings("unchecked")
            DRBasedClassAdder gdra = new DRBasedClassAdder(drInformation[0], drInformation[1]);
            StreamRDF drStream = new RDFStreamTripleFlatMapper(gdra, new StreamRDF2(outStream, typeStream));

            // The type stream is only interested in the rdf:type triples with dbo classes;
            // all other triples are forwarded to the dr stream
            StreamRDF stream = new RDFStreamTripleFilter(
                    new NodeFilterBasedTripleFilter(null, new EqualityNodeFilter(RDF.type.asNode()),
                            new StringBasedNamespaceNodeFilter("http://dbpedia.org/ontology/")),
                    typeStream, drStream);

            // Add monitor at the beginning of the stream
            ProgressMonitor monitorS = new ProgressMonitorOutput("Processed triples", 100000, 10,
                    MonitorOutputs.outputToLog(LOGGER));
            stream = new ProgressStreamRDF(stream, monitorS);

            // Start reading triples from the input file
            monitorS.start();
            monitor1.start();
            stream.start();
            RDFParser.source(inputFile).lang(Lang.NT).parse(stream);
            monitorS.finish();
            monitor1.finish();
            stream.finish();
            LOGGER.info("Finished");
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ArrayList<String>> readClassHierarchy(File file) throws IOException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(HashMap.class, new ComplexHashMapDeserializer(HashMap.class));
        ObjectMapper mapper = new ObjectMapper().registerModule(module);
        return mapper.readValue(file, HashMap.class);
    }
}
