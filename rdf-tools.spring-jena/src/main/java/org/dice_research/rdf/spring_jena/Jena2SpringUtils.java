package org.dice_research.rdf.spring_jena;

import java.nio.charset.Charset;

import org.apache.jena.atlas.web.ContentType;
import org.springframework.http.MediaType;

public class Jena2SpringUtils {

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
}
