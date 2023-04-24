package org.dice_research.rdf.examples;

import java.io.File;
import java.io.IOException;

import org.dice_research.rdf.test.ModelComparisonHelper;
import org.dice_research.test.FileResourceUtils;
import org.junit.Test;

public class FactBench2ReifiedConverterTest {

    @Test
    public void test() throws IOException {
        File trueDirectory = FileResourceUtils.getFile("FactBench2ReifiedConverterTest/True", this.getClass());
        File falseDirectory = FileResourceUtils.getFile("FactBench2ReifiedConverterTest/False", this.getClass());
        File expectedNewFile = FileResourceUtils.getFile("FactBench2ReifiedConverterTest/expected.nt", this.getClass());
        File resultFile = File.createTempFile("FactBench2ReifiedConverterTest", ".nt");

        FactBench2ReifiedConverter
                .main(new String[] { trueDirectory.getAbsolutePath(), falseDirectory.getAbsolutePath(),
                        "http://example.org/FC2ReifiedConverterTest/", resultFile.getAbsolutePath() });

        ModelComparisonHelper.assertModelsEqual(expectedNewFile.toURI().toURL(), resultFile.toURI().toURL());
    }
}
