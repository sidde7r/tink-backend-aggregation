package se.tink.backend.aggregation.agents.utils.berlingroup.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.Href;

public class LinkDeserializer extends JsonDeserializer<String> {

    @Override
    @SneakyThrows
    public String deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        JsonToken jsonToken = parser.getCurrentToken();
        if (jsonToken == JsonToken.VALUE_STRING) {
            return parser.getValueAsString();
        } else if (jsonToken == JsonToken.START_OBJECT) {
            Href href = context.readValue(parser, Href.class);
            return href == null ? null : href.getHref();
        }
        return null;
    }
}
