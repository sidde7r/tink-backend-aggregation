package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import javax.ws.rs.core.MediaType;
import org.assertj.core.util.Strings;

public class BodyEntityFactory {

    public static BodyEntity create(String rawData, MediaType mediaType) {

        if (Strings.isNullOrEmpty(rawData)) {
            return new EmptyBodyEntity();
        }

        rawData = rawData.trim();

        MediaType mediaTypeWithoutParams =
                new MediaType(mediaType.getType(), mediaType.getSubtype());
        if (mediaTypeWithoutParams.equals(MediaType.APPLICATION_JSON_TYPE)) {
            if (rawData.startsWith("[")) {
                return new JSONListBodyEntity(rawData);
            }
            return new JSONMapBodyEntity(rawData);
        }

        if (mediaTypeWithoutParams.equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
            return new URLEncodedBodyEntity(rawData);
        }

        if (mediaTypeWithoutParams.equals(MediaType.TEXT_XML_TYPE)) {
            return new XMLBodyEntity(rawData);
        }

        if (mediaTypeWithoutParams.equals(MediaType.TEXT_PLAIN_TYPE)) {
            return new PlainTextBodyEntity(rawData);
        }

        throw new IllegalStateException(
                "Could not parse request body, unsupported content-type " + mediaType.toString());
    }
}
