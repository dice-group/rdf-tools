package org.dice_research.rdf.examples;

import java.io.File;
import java.io.IOException;

import org.dice_research.rdf.test.ModelComparisonHelper;
import org.dice_research.test.FileResourceUtils;
import org.junit.Test;

public class WikidataPreprocessingTest {

    @Test
    public void testPlainNT() throws IOException {
        test(FileResourceUtils.getFile("WikidataPreprocessingTest/wikidata-dump-head.nt", this.getClass()));
    }

    @Test
    public void testGZipped() throws IOException {
        test(FileResourceUtils.getFile("WikidataPreprocessingTest/wikidata-dump-head.nt.gz", this.getClass()));
    }

    @Test
    public void testBZipped() throws IOException {
        test(FileResourceUtils.getFile("WikidataPreprocessingTest/wikidata-dump-head.nt.bz2", this.getClass()));
    }

    protected void test(File inputFile) throws IOException {
        File expectedFile = FileResourceUtils.getFile("WikidataPreprocessingTest/expected.nt", this.getClass());
        File resultFile = File.createTempFile("WikidataPreprocessingTest", ".nt");

        WikidataPreprocessing.main(new String[] { inputFile.getAbsolutePath(), resultFile.getAbsolutePath() });

        ModelComparisonHelper.assertModelsEqual(expectedFile.toURI().toURL(), resultFile.toURI().toURL());
    }
}
