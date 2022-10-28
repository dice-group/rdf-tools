package org.dice_research.rdf.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.dice_research.serial.maps.ComplexHashMapDeserializer;
import org.dice_research.test.CollectionsComparisonHelper;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class DomainRangeCollectorTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws URISyntaxException, IOException {
        URL fileUrl = this.getClass().getClassLoader().getResource("Example.nt");

        Map<String, Set<String>> expectedDomains = new HashMap<>();
        expectedDomains.put("http://example.org/properties/p1",
                new HashSet<>(Arrays.asList("http://example.org/class/1111")));
        expectedDomains.put("http://example.org/properties/p1223",
                new HashSet<>(Arrays.asList("http://example.org/class/1", "http://example.org/class/2")));
        Map<String, Set<String>> expectedRanges = new HashMap<>();
        expectedRanges.put("http://example.org/properties/p1",
                new HashSet<>(Arrays.asList("http://example.org/class/1111")));
        expectedRanges.put("http://example.org/properties/p1223",
                new HashSet<>(Arrays.asList("http://example.org/class/2", "http://example.org/class/3")));
        expectedRanges.put("http://example.org/properties/p3",
                new HashSet<>(Arrays.asList("http://www.w3.org/2001/XMLSchema#string")));

        File inputFile = new File(fileUrl.toURI());
        File tempOutputFile = File.createTempFile("test_", ".json");

        // Run the collector
        DomainRangeCollector.main(new String[] { inputFile.getAbsolutePath(), tempOutputFile.getAbsolutePath() });

        try (InputStream is = new FileInputStream(tempOutputFile.getAbsolutePath())) {
            IOUtils.copy(is, System.out);
        }

        // Read the result
        SimpleModule module = new SimpleModule();
        module.addDeserializer(HashMap.class, new ComplexHashMapDeserializer(HashMap.class));
        ObjectMapper mapper = new ObjectMapper().registerModule(module);
        @SuppressWarnings("rawtypes")
        HashMap[] readResult = mapper.readValue(tempOutputFile, HashMap[].class);

        // Check the result
        HashMap<String, Set<String>> readMap = readResult[0];
        CollectionsComparisonHelper.assertSetsEqual(expectedDomains.keySet(), readMap.keySet(), String[]::new);
        for (String key : expectedDomains.keySet()) {
            CollectionsComparisonHelper.assertSetsEqual(expectedDomains.get(key), readMap.get(key), String[]::new);
        }
        readMap = readResult[1];
        CollectionsComparisonHelper.assertSetsEqual(expectedRanges.keySet(), readMap.keySet(), String[]::new);
        for (String key : expectedRanges.keySet()) {
            CollectionsComparisonHelper.assertSetsEqual(expectedRanges.get(key), readMap.get(key), String[]::new);
        }
    }
}
