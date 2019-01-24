package se.tink.backend.aggregation.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import se.tink.libraries.strings.StringUtils;

import java.io.IOException;

public class TrimmingStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String value = jp.getValueAsString();
        if (value != null) {
            return StringUtils.trim(value);
        }

        return null;
    }
}
