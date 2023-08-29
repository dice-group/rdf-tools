package org.dice_research.rdf.spring_jena;

import java.nio.charset.Charset;
import java.util.stream.Stream;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.WebContent;
import org.springframework.http.MediaType;

/**
 * A class with utility methods that ease to connect the Jena and Spring
 * libraries.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class Jena2SpringUtils {

    private Jena2SpringUtils() {
        // We don't need an instance of this class
    }

    /**
     * Array containing all media type strings supported by the classes of this
     * library
     */
    public final static String[] SUPPORTED_MEDIA_TYPES = new String[] { WebContent.contentTypeJSONLD,
            WebContent.contentTypeTurtle, WebContent.contentTypeTurtleAlt1, WebContent.contentTypeRDFXML,
            WebContent.contentTypeRDFJSON, WebContent.contentTypeTextPlain, WebContent.contentTypeNTriples,
            WebContent.contentTypeNTriplesAlt, WebContent.contentTypeXML, WebContent.contentTypeXMLAlt,
            WebContent.contentTypeTriG, WebContent.contentTypeNQuads, WebContent.contentTypeTriGAlt1,
            WebContent.contentTypeRDFProto, WebContent.contentTypeRDFThrift, WebContent.contentTypeNQuadsAlt1,
            WebContent.contentTypeTriX, WebContent.contentTypeTriXxml, WebContent.contentTypeN3,
            WebContent.contentTypeN3Alt1, WebContent.contentTypeN3Alt2 };

    /**
     * Function to transform the given {@link ContentType} instance into a
     * {@link MediaType} instance.
     * 
     * @param contentType the content type that should be transformed into a media
     *                    type
     * @return the media type expressing the same information as the given content
     *         type
     */
    public static MediaType contentType2MediaType(ContentType contentType) {
        Charset charset = null;
        if (contentType.getCharset() != null) {
            try {
                charset = Charset.forName(contentType.getCharset());
            } catch (Exception e) {
                // nothing to do
            }
        }
        if (charset != null) {
            return new MediaType(contentType.getType(), contentType.getSubType(), charset);
        } else {
            return new MediaType(contentType.getType(), contentType.getSubType());
        }
    }

    /**
     * This function transforms the given {@link Lang} instances into the content
     * type strings that they represent.
     * 
     * @param langs
     * @return
     */
    public static String[] langs2MediaTypeStrings(Lang... langs) {
        return Stream.of(langs).flatMap(l -> l.getAltContentTypes().stream()).toArray(String[]::new);
    }
}
