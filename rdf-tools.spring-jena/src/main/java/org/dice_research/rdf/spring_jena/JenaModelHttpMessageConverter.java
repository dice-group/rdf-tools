package org.dice_research.rdf.spring_jena;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * A message converter instance that implements the serialization and
 * deserialization of {@link Model} instances.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class JenaModelHttpMessageConverter extends AbstractHttpMessageConverter<Model> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenaModelHttpMessageConverter.class);

    public static final Lang SUPPORTED_LANGS[] = new Lang[] { Lang.JSONLD, Lang.JSONLD10, Lang.JSONLD11, Lang.NQUADS,
            Lang.NTRIPLES, Lang.RDFJSON, Lang.RDFPROTO, Lang.RDFTHRIFT, Lang.RDFXML, Lang.TRIG, Lang.TRIX,
            Lang.TURTLE };

    /**
     * The default {@link Lang} used for (de-)serialization.
     */
    protected Lang defaultLang;
    /**
     * The media type of the {@link #defaultLang}.
     */
    protected MediaType defaultMediaType;
    /**
     * Mapping of media types to {@link Lang} instances.
     */
    protected Map<MediaType, Lang> type2Lang;

    /**
     * Constructor.
     * 
     * @param defaultLang The default {@link Lang} used for (de-)serialization.
     * @param type2Lang   Mapping of media types to {@link Lang} instances.
     */
    public JenaModelHttpMessageConverter(Lang defaultLang, Map<MediaType, Lang> type2Lang) {
        this(defaultLang, Jena2SpringUtils.contentType2MediaType(defaultLang.getContentType()), type2Lang);
    }

    /**
     * Constructor.
     * 
     * @param defaultLang      The default {@link Lang} used for (de-)serialization.
     * @param defaultMediaType The media type of the {@link #defaultLang}.
     * @param type2Lang        Mapping of media types to {@link Lang} instances.
     */
    public JenaModelHttpMessageConverter(Lang defaultLang, MediaType defaultMediaType, Map<MediaType, Lang> type2Lang) {
        super(StandardCharsets.UTF_8, type2Lang.keySet().toArray(MediaType[]::new));
        this.defaultLang = defaultLang;
        this.defaultMediaType = defaultMediaType;
        this.type2Lang = type2Lang;
    }

    @Override
    protected MediaType getDefaultContentType(Model t) throws IOException {
        return defaultMediaType;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        // If the given class is the Model interface (typically the case if we want to
        // read data)
        if (Model.class.equals(clazz)) {
            return true;
        }
        // Check if the given class implements the Model interface /(typically the case
        // if we want to write data)
        return Stream.of(clazz.getInterfaces()).filter(c -> Model.class.equals(c)).findFirst().isPresent();
    }

    @Override
    protected Model readInternal(Class<? extends Model> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        // Choose language based on content type
        MediaType type = inputMessage.getHeaders().getContentType();
        Lang lang = null;
        if (type == null) {
            LOGGER.warn("Content-type not set for incoming message. I will use the default language {}.", defaultLang);
            lang = defaultLang;
        } else {
            // If there is a charset given, remove it; otherwise, we will not find the media
            // type in our map
            if (type.getCharset() != null) {
                type = new MediaType(type.getType(), type.getSubtype());
            }
            lang = type2Lang.get(type);
            if (lang == null) {
                throw new HttpMessageNotReadableException("Unsupported content type " + type.toString(), inputMessage);
            }
        }
        Model result = ModelFactory.createDefaultModel();
        try {
            RDFDataMgr.read(result, inputMessage.getBody(), lang);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpMessageNotReadableException("Exception while parsing RDF data.", e, inputMessage);
        }
        return result;
    }

    @Override
    protected void writeInternal(Model model, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        // Choose language based on content type
        MediaType type = outputMessage.getHeaders().getContentType();
        Lang lang = null;
        if (type == null) {
            LOGGER.warn("Content-type not set for outgoing message. I will use the default language {}.", defaultLang);
            lang = defaultLang;
        } else {
            // If there is a charset given, remove it; otherwise, we will not find the media
            // type in our map
            if (type.getCharset() != null) {
                type = new MediaType(type.getType(), type.getSubtype());
            }
            lang = type2Lang.get(type);
            if (lang == null) {
                throw new HttpMessageNotWritableException("Unsupported content type " + type.toString());
            }
        }
        try {
            RDFDataMgr.write(outputMessage.getBody(), model, lang);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new HttpMessageNotWritableException("Exception while writing RDF data.", e);
        }
    }

    /**
     * Factory method that generates a converter that supports all of the given
     * {@link Lang} instances and uses the first of them as default.
     * 
     * @param langs The {@link Lang} instances that are supported by the created
     *              converter
     * @return A new converter for the given {@link Lang} instances
     */
    public static JenaModelHttpMessageConverter create(Lang... langs) {
        if (langs.length == 0) {
            throw new IllegalArgumentException("The converter class needs at least one valid serialization language.");
        }
        return create(langs[0], langs);
    }

    /**
     * Factory method that generates a converter that supports all of the given
     * {@link Lang} instances and uses the given single {@link Lang} instance as
     * default.
     * 
     * @param defaultLang The default {@link Lang} used for (de-)serialization.
     * @param langs       The {@link Lang} instances that are supported by the
     *                    created converter
     * @return A new converter for the given {@link Lang} instances
     */
    public static JenaModelHttpMessageConverter create(Lang defaultLang, Lang... langs) {
        return create(defaultLang, null, langs);
    }

    /**
     * Factory method that generates a converter that supports all of the given
     * {@link Lang} instances and media types which abide to the given regular
     * expression.
     * 
     * @param defaultLang  The default {@link Lang} used for (de-)serialization.
     * @param regexPattern a regular expression that can be used to filter the media
     *                     types of the {@link Lang} instances
     * @param langs        The {@link Lang} instances that are supported by the
     *                     created converter
     * @return A new converter for the given {@link Lang} instances
     */
    public static JenaModelHttpMessageConverter create(Lang defaultLang, String regexPattern, Lang... langs) {
        Map<MediaType, Lang> type2Lang = new HashMap<>();
        defaultLang.getAltContentTypes().stream().filter(s -> regexPattern == null || s.matches(regexPattern))
                .map(s -> MediaType.parseMediaType(s)).forEach(m -> type2Lang.put(m, defaultLang));
        for (int i = 0; i < langs.length; ++i) {
            if (!defaultLang.equals(langs[i])) {
                langs[i].getAltContentTypes().stream().filter(s -> regexPattern == null || s.matches(regexPattern))
                        .map(s -> MediaType.parseMediaType(s)).filter(m -> !type2Lang.containsKey(m))
                        .forEach(m -> type2Lang.put(m, defaultLang));
            }
        }
        return new JenaModelHttpMessageConverter(defaultLang, type2Lang);
    }

    /**
     * Factory method that generates a converter that supports all
     * {@link #SUPPORTED_LANGS} that have a mediatype starting with
     * {@code "application/"}. The given {@link Lang} instance will be used as
     * default. At the time of writing, this could be one of the following:
     * <ul>
     * <li>application/ld+json</li>
     * <li>application/n-quads</li>
     * <li>application/n-triples</li>
     * <li>application/rdf+json</li>
     * <li>application/rdf+protobuf</li>
     * <li>application/rdf+thrift</li>
     * <li>application/rdf+xml</li>
     * <li>application/trig</li>
     * <li>application/trix+xml</li>
     * <li>application/trix</li>
     * <li>application/turtle</li></li>
     * </ul>
     * 
     * @param defaultLang The default {@link Lang} used for (de-)serialization.
     * @return A new converter for all {@link #SUPPORTED_LANGS} that have a
     *         mediatype starting with {@code "application/"}.
     */
    public static JenaModelHttpMessageConverter createForApplicationMediaTypes(Lang defaultLang) {
        return create(defaultLang, "application/.*", SUPPORTED_LANGS);
    }

    /**
     * Factory method that generates a converter that supports all
     * {@link #SUPPORTED_LANGS} that have a mediatype starting with {@code "text/"}.
     * The given {@link Lang} instance will be used as default. At the time of
     * writing, this could be one of the following:
     * <ul>
     * <li>text/n-quads</li>
     * <li>text/plain</li>
     * <li>text/trig</li>
     * <li>text/turtle</li>
     * </ul>
     * 
     * @param defaultLang The default {@link Lang} used for (de-)serialization.
     * @return A new converter for all {@link #SUPPORTED_LANGS} that have a
     *         mediatype starting with {@code "text/"}.
     */
    public static JenaModelHttpMessageConverter createForTextMediaTypes(Lang defaultLang) {
        return create(defaultLang, "text/.*", SUPPORTED_LANGS);
    }

    /**
     * Factory method that generates a converter that supports all
     * {@link #SUPPORTED_LANGS} that have a mediatype starting with {@code "x/"}.
     * The given {@link Lang} instance will be used as default. At the time of
     * writing, this could be one of the following:
     * <ul>
     * <li>x/ld-json-10</li>
     * <li>x/ld-json-11</li>
     * </ul>
     * 
     * @param defaultLang The default {@link Lang} used for (de-)serialization.
     * @return A new converter for all {@link #SUPPORTED_LANGS} that have a
     *         mediatype starting with {@code "text/"}.
     */
    public static JenaModelHttpMessageConverter createForUserDefinedMediaTypes(Lang defaultLang) {
        return create(defaultLang, "x/.*", SUPPORTED_LANGS);
    }

}
