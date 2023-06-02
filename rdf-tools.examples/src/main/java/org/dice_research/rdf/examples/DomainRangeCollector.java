package org.dice_research.rdf.examples;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.system.progress.MonitorOutputs;
import org.apache.jena.system.progress.ProgressMonitor;
import org.apache.jena.system.progress.ProgressMonitorOutput;
import org.apache.jena.system.progress.ProgressStreamRDF;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.rdf.stream.collect.RDFStreamGroupByCollector;
import org.dice_research.rdf.stream.filter.RDFStreamTripleFilter;
import org.dice_research.serial.maps.ComplexMapSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class DomainRangeCollector extends StreamRDFBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassHierarchyCollector.class);

    public static StreamRDF createStream(Map<String, Set<String>> domains, Map<String, Set<String>> ranges) {
        // We collect the IRI of the subject and object and assume that the object is
        // the range of the subject
        StreamRDF stream1 = new RDFStreamGroupByCollector<>(t -> t.getSubject().getURI(), t -> t.getObject().getURI(),
                HashSet::new, ranges);
        // Hence, we should make sure that we only have rdfs:range triples
        final String RDFS_RANGE = RDFS.range.getURI();
        stream1 = new RDFStreamTripleFilter(t -> RDFS_RANGE.equals(t.getPredicate().getURI()), stream1);

        // We collect the IRI of the subject and object and assume that the object is
        // the domain of the subject
        StreamRDF stream2 = new RDFStreamGroupByCollector<>(t -> t.getSubject().getURI(), t -> t.getObject().getURI(),
                HashSet::new, domains);
        // Hence, we should make sure that we only have rdfs:domain triples; we should also forward all neglected triples to stream1
        final String RDFS_DOMAIN = RDFS.domain.getURI();
        stream2 = new RDFStreamTripleFilter(t -> RDFS_DOMAIN.equals(t.getPredicate().getURI()), stream2, stream1);
        return stream2;
    }

    @SuppressWarnings("unchecked")
    protected static Set<String> propagate(Map<String, Set<String>> classHierarchy, String element,
            Set<String> visitedElements) {
        visitedElements.add(element);
        if (classHierarchy.containsKey(element)) {
            Set<String> superClasses = classHierarchy.get(element);
            Set<String> derivedSuperClasses;
            Set<String> classesToAdd = null;
            for (String key : superClasses) {
                derivedSuperClasses = propagate(classHierarchy, key, visitedElements);
                if (derivedSuperClasses.size() > 0) {
                    if (classesToAdd == null) {
                        classesToAdd = new HashSet<>();
                    }
                    classesToAdd.addAll(derivedSuperClasses);
                }
            }
            // If we found classes that may not be part of the set already, add them
            if (classesToAdd != null) {
                superClasses.addAll(classesToAdd);
            }
            return superClasses;
        } else {
            return (Set<String>) Collections.EMPTY_SET;
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Error: wrong usage. DomainRangeCollector <input-file> <output-file>");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];

        // Create the maps
        HashMap<String, Set<String>> domains = new HashMap<>();
        HashMap<String, Set<String>> ranges = new HashMap<>();

        // Create stream starting from the end!
        StreamRDF stream = createStream(domains, ranges);

        // Add monitor at the beginning of the stream
        ProgressMonitor monitorS = new ProgressMonitorOutput("Processed triples", 100000, 10,
                MonitorOutputs.outputToLog(LOGGER));
        stream = new ProgressStreamRDF(stream, monitorS);

        LOGGER.info("Streaming data...");
        // Start reading triples from the input file
        monitorS.start();
        stream.start();
        RDFParser.source(inputFile).lang(Lang.NT).parse(stream);
        monitorS.finish();
        stream.finish();

        // Write the maps to a file
        SimpleModule module = new SimpleModule();
        module.addSerializer(HashMap.class, new ComplexMapSerializer());
        module.addSerializer(Map.class, new ComplexMapSerializer());
        ObjectMapper mapper = new ObjectMapper().registerModule(module);
        try {
            mapper.writeValue(new File(outputFile), new HashMap[] {domains, ranges});
        } catch (Exception e) {
            LOGGER.error("Error while writing domains and ranges to file.");
        }

        LOGGER.info("Finished");
    }
}
