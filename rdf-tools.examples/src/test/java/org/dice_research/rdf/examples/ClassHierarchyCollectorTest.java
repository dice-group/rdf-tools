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

public class ClassHierarchyCollectorTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws URISyntaxException, IOException {
        URL fileUrl = this.getClass().getClassLoader().getResource("Example.nt");
        
        Map<String, Set<String>> expectedHierarchy = new HashMap<>();
        expectedHierarchy.put("http://example.org/class/1", new HashSet<>());
        expectedHierarchy.put("http://example.org/class/2", new HashSet<>());
        expectedHierarchy.put("http://example.org/class/3", new HashSet<>());
        expectedHierarchy.put("http://example.org/class/11", new HashSet<>(Arrays.asList("http://example.org/class/1")));
        expectedHierarchy.put("http://example.org/class/12", new HashSet<>(Arrays.asList("http://example.org/class/1","http://example.org/class/2")));
        expectedHierarchy.put("http://example.org/class/13", new HashSet<>(Arrays.asList("http://example.org/class/1","http://example.org/class/3")));
        expectedHierarchy.put("http://example.org/class/111", new HashSet<>(Arrays.asList("http://example.org/class/1","http://example.org/class/11")));
        expectedHierarchy.put("http://example.org/class/1111", new HashSet<>(Arrays.asList("http://example.org/class/1","http://example.org/class/11","http://example.org/class/111")));
        expectedHierarchy.put("http://example.org/class/11111", new HashSet<>(Arrays.asList("http://example.org/class/1","http://example.org/class/11","http://example.org/class/111","http://example.org/class/1111")));
        
        File inputFile = new File(fileUrl.toURI());
        File tempOutputFile = File.createTempFile("test_", ".json");
        
        // Run the collector
        ClassHierarchyCollector.main(new String[] {inputFile.getAbsolutePath(), tempOutputFile.getAbsolutePath()});
        
        try (InputStream is = new FileInputStream(tempOutputFile.getAbsolutePath())) {
            IOUtils.copy(is, System.out);
        }
        
        // Read the result
        SimpleModule module = new SimpleModule();
        module.addDeserializer(HashMap.class, new ComplexHashMapDeserializer(HashMap.class));
        ObjectMapper mapper = new ObjectMapper().registerModule(module);
        HashMap<String, Set<String>> readResult = mapper.readValue(tempOutputFile, HashMap.class);
        
        // Check the result
        CollectionsComparisonHelper.assertSetsEqual(expectedHierarchy.keySet(), readResult.keySet(), String[]::new);
        for(String key : expectedHierarchy.keySet()) {
            CollectionsComparisonHelper.assertSetsEqual(expectedHierarchy.get(key), readResult.get(key), String[]::new);
        }
    }
    
}
