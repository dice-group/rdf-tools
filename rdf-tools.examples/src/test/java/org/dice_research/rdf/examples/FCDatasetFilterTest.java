package org.dice_research.rdf.examples;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.dice_research.rdf.test.ModelComparisonHelper;
import org.junit.Assert;
import org.junit.Test;

public class FCDatasetFilterTest {

    @Test
    public void test() throws IOException {
        File oldFile = getFile("FCDatasetFilterTest/old-data.nt");
        File largeFile = getFile("FCDatasetFilterTest/large-data.nt");
        File expectedNewFile = getFile("FCDatasetFilterTest/expected-new-data.nt");
        File resultFile = File.createTempFile("FCDatasetFilterTestResult", ".nt");

        FCDatasetFilter.main(
                new String[] { oldFile.getAbsolutePath(), largeFile.getAbsolutePath(), resultFile.getAbsolutePath() });

        ModelComparisonHelper.assertModelsEqual(expectedNewFile.toURI().toURL(), resultFile.toURI().toURL());
    }

    private File getFile(String resourceName) {
        URL url = this.getClass().getClassLoader().getResource(resourceName);
        Assert.assertNotNull(url);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Got an unexpected error while processing the given resource name.", e);
        }
    }
}
