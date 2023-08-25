package org.dice_research.rdf.spring_jena;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class JenaModelHttpMessageConverter extends AbstractHttpMessageConverter<Model>
        implements HttpMessageConverter<Model> {

    protected Lang lang;

    public JenaModelHttpMessageConverter(Lang lang, MediaType mediaType) {
        super(StandardCharsets.UTF_8, mediaType);
        this.lang = lang;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Model.class.equals(clazz);
    }

    @Override
    protected Model readInternal(Class<? extends Model> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        Model result = ModelFactory.createDefaultModel();
        RDFDataMgr.read(result, inputMessage.getBody(), lang);
        return result;
    }

    @Override
    protected void writeInternal(Model model, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        RDFDataMgr.write(outputMessage.getBody(), model, lang);
    }

    public Lang getLang() {
        return lang;
    }

    public static List<JenaModelHttpMessageConverter> generateConverters() {
        // Warning! Streaming all languages available does not make much sense, since
        // not all of them can be used for Model objects
        // RDFLanguages.getRegisteredLanguages().stream()

        // Get a converter for every media type that a chosen set of languages have
        return Stream.of(Lang.JSONLD, Lang.JSONLD10, Lang.JSONLD11, Lang.NQUADS, Lang.NTRIPLES, Lang.RDFJSON,
                Lang.RDFPROTO, Lang.RDFTHRIFT, Lang.RDFXML, Lang.TRIG, Lang.TRIX, Lang.TURTLE).flatMap(l ->
        // Originally, we had a conversion of the main media type and the alternatives.
        // However, the main type is listed as alternative. So we only use alternatives,
        // if there are more than one.
        l.getAltContentTypes().size() > 1
                ? l.getAltContentTypes().stream()
                        .map(a -> new JenaModelHttpMessageConverter(l, MediaType.parseMediaType(a)))
                : Stream.of(new JenaModelHttpMessageConverter(l,
                        Jena2SpringUtils.contentType2MediaType(l.getContentType()))))
                .collect(Collectors.toList());
    }
}
