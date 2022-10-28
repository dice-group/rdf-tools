package org.dice_research.java.stream;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class PredicateConsumerCombinerTest {

    @Test
    public void test() {
        Set<String> collector = new HashSet<String>();
        PredicateConsumerCombiner<String> combiner = new PredicateConsumerCombiner<>(s -> s.startsWith("http"),
                s -> collector.add(s));
        
        combiner.accept("http://google.com");
        Assert.assertTrue(collector.contains("http://google.com"));
        collector.clear();

        combiner.accept("https://wikipedia.org");
        Assert.assertTrue(collector.contains("https://wikipedia.org"));
        collector.clear();

        combiner.accept("http://dbpedia.org/resource");
        Assert.assertTrue(collector.contains("http://dbpedia.org/resource"));
        collector.clear();

        combiner.accept("42");
        Assert.assertEquals(0, collector.size());
        collector.clear();

        combiner.accept("This should fail");
        Assert.assertEquals(0, collector.size());
        collector.clear();
    }
}
