package org.dice_research.rdf.examples;

import java.io.File;
import java.io.IOException;

import org.dice_research.rdf.test.ModelComparisonHelper;
import org.dice_research.test.FileResourceUtils;
import org.junit.Test;

public class FCDatasetFilterTest {

    @Test
    public void test() throws IOException {
        File oldFile = FileResourceUtils.getFile("FCDatasetFilterTest/old-data.nt", this.getClass());
        File largeFile = FileResourceUtils.getFile("FCDatasetFilterTest/large-data.nt", this.getClass());
        File expectedNewFile = FileResourceUtils.getFile("FCDatasetFilterTest/expected-new-data.nt", this.getClass());
        File resultFile = File.createTempFile("FCDatasetFilterTestResult", ".nt");

        FCDatasetFilter.main(
                new String[] { oldFile.getAbsolutePath(), largeFile.getAbsolutePath(), resultFile.getAbsolutePath() });

        ModelComparisonHelper.assertModelsEqual(expectedNewFile.toURI().toURL(), resultFile.toURI().toURL());
    }

}
