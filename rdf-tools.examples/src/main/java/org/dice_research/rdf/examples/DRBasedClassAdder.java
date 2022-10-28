package org.dice_research.rdf.examples;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.serial.maps.ComplexHashMapDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class DRBasedClassAdder implements Function<Triple, Stream<Triple>> {

    protected Map<String, Set<String>> domainInfo;
    protected Map<String, Set<String>> rangeInfo;

    public DRBasedClassAdder(Map<String, Set<String>> domainInfo, Map<String, Set<String>> rangeInfo) {
        this.domainInfo = domainInfo;
        this.rangeInfo = rangeInfo;
    }

    @Override
    public Stream<Triple> apply(Triple t) {
        String predicate = t.getPredicate().getURI();
        Node subject = t.getSubject();
        Node object = t.getObject();
        if (domainInfo.containsKey(predicate)) {
            Stream<Triple> result;
            // Generate a stream of classes for the subject
            result = domainInfo.get(predicate).stream().map(NodeFactory::createURI)
                    .map(c -> new Triple(subject, RDF.type.asNode(), c));
            if (!object.isLiteral() && rangeInfo.containsKey(predicate)) {
                // Generate a stream of classes for the object
                result = Stream.concat(result, rangeInfo.get(predicate).stream().map(NodeFactory::createURI)
                        .map(c -> new Triple(object, RDF.type.asNode(), c)));
            }
            return result;
        } else {
            if (!object.isLiteral() && rangeInfo.containsKey(predicate)) {
                return rangeInfo.get(predicate).stream().map(NodeFactory::createURI)
                        .map(c -> new Triple(object, RDF.type.asNode(), c));
            }
        }
        return Stream.empty();
    }

    @SuppressWarnings("rawtypes")
    public static HashMap[] readDRInformation(File file) throws IOException {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(HashMap.class, new ComplexHashMapDeserializer(HashMap.class));
        ObjectMapper mapper = new ObjectMapper().registerModule(module);
        return mapper.readValue(file, HashMap[].class);
    }

}
