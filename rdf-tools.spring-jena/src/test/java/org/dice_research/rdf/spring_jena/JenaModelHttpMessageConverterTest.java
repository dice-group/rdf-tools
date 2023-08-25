package org.dice_research.rdf.spring_jena;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.dice_research.rdf.test.ModelComparisonHelper;
import org.dice_research.rdf.test.ModelResourceUtils;
import org.junit.Assert;
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
    private Model expectedModel;

    public JenaModelHttpMessageConverterTest(JenaModelHttpMessageConverter converter, Model expectedModel) {
        super();
        this.converter = converter;
        this.expectedModel = expectedModel;
        System.out.println("Testing converter for " + converter.getSupportedMediaTypes().toString());
    }

    @Test
    public void test() throws HttpMessageNotWritableException, IOException {
        Assert.assertEquals("This converter supports more media types than expected!", 1,
                converter.getSupportedMediaTypes().size());
        MediaType mediaType = converter.getSupportedMediaTypes().get(0);

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
        // Generate test cases
        List<Object[]> testCases = JenaModelHttpMessageConverter.generateConverters().stream()
                .map(c -> new Object[] { c, testModel }).collect(Collectors.toList());

        return testCases;
    }
}
