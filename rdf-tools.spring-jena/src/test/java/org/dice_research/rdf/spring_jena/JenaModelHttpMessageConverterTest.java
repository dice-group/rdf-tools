package org.dice_research.rdf.spring_jena;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.dice_research.rdf.test.ModelComparisonHelper;
import org.dice_research.rdf.test.ModelResourceUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

@RunWith(Parameterized.class)
public class JenaModelHttpMessageConverterTest {

    private JenaModelHttpMessageConverter converter;
    private MediaType mediaType;
    private Model expectedModel;

    public JenaModelHttpMessageConverterTest(JenaModelHttpMessageConverter converter, MediaType mediaType,
            Model expectedModel) {
        super();
        this.converter = converter;
        this.mediaType = mediaType;
        this.expectedModel = expectedModel;
        System.out.println("Testing converter for " + converter.getSupportedMediaTypes().toString());
    }

    @Test
    public void test() throws HttpMessageNotWritableException, IOException {
        MockHttpOutputMessage outMessage = new MockHttpOutputMessage();
        converter.write(expectedModel, mediaType, outMessage);

        MockHttpInputMessage inMessage = new MockHttpInputMessage(outMessage.getBodyAsBytes());
        Model readModel = converter.read(Model.class, inMessage);

        ModelComparisonHelper.assertModelsEqual(expectedModel, readModel);
    }

    @Parameters
    public static List<Object[]> parameters() {
        // Read test data from resources
        final Model testModel = ModelResourceUtils.loadModel(JenaModelHttpMessageConverterTest.class.getClassLoader(),
                "example.nt", Lang.NT);

        List<Object[]> testCases = new ArrayList<>();

        // Generate test cases with single languages
        Lang langs[] = JenaModelHttpMessageConverter.SUPPORTED_LANGS;
        List<MediaType> types = new ArrayList<>();
        MediaType type;
        List<String> contentTypes;
        for (int i = 0; i < langs.length; ++i) {
            contentTypes = langs[i].getAltContentTypes();
            for (String contentType : contentTypes) {
                type = MediaType.parseMediaType(contentType);
                testCases.add(new Object[] {
                        new JenaModelHttpMessageConverter(langs[i], type, Collections.singletonMap(type, langs[i])),
                        type, testModel });
                types.add(type);
            }
        }

        // Generate test cases for a generic converter
        JenaModelHttpMessageConverter genericConverter = JenaModelHttpMessageConverter.create(langs);
        for (MediaType mType : types) {
            testCases.add(new Object[] { genericConverter, mType, testModel });
        }
        // Add a test case with no media type
        testCases.add(new Object[] { genericConverter, null, testModel });

        return testCases;
    }
}
