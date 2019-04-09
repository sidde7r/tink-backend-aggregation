package se.tink.backend.aggregation.agents.banks.nordea;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import java.io.IOException;
import se.tink.libraries.date.DateUtils;

public class NordeaHashMapDeserializer extends JsonDeserializer<java.lang.String> {
    @Override
    public java.lang.String deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {
        JsonNode jsonNode = contentNode(jsonParser);
        return jsonNode != null ? jsonNode.asText() : null;
    }

    public static class String extends JsonDeserializer<java.lang.String> {
        @Override
        public java.lang.String deserialize(JsonParser jsonParser, DeserializationContext context)
                throws IOException {
            JsonNode jsonNode = contentNode(jsonParser);
            return jsonNode != null ? jsonNode.asText() : null;
        }
    }

    public static class Double extends JsonDeserializer<java.lang.Double> {
        @Override
        public java.lang.Double deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            JsonNode jsonNode = contentNode(jsonParser);
            return jsonNode != null ? jsonNode.asDouble() : null;
        }
    }

    public static class Boolean extends JsonDeserializer<java.lang.Boolean> {
        @Override
        public java.lang.Boolean deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            JsonNode jsonNode = contentNode(jsonParser);

            if (jsonNode == null) {
                return null;
            }

            java.lang.String asText = jsonNode.asText();
            switch (asText.toLowerCase()) {
                case "yes":
                case "true":
                    return true;
                default:
                    return false;
            }
        }
    }

    public static class Date extends JsonDeserializer<java.util.Date> {
        @Override
        public java.util.Date deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            JsonNode jsonNode = contentNode(jsonParser);

            if (jsonNode == null) {
                return null;
            }

            java.lang.String dateString = jsonNode.asText();
            if (Strings.isNullOrEmpty(dateString)) {
                return null;
            }

            return DateUtils.parseDate(dateString);
        }
    }

    public static class Integer extends JsonDeserializer<java.lang.Integer> {
        @Override
        public java.lang.Integer deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            JsonNode jsonNode = contentNode(jsonParser);
            return jsonNode != null ? jsonNode.asInt() : null;
        }
    }

    private static JsonNode contentNode(JsonParser jsonParser) throws IOException {
        ObjectCodec objectCodec = jsonParser.getCodec();
        JsonNode node = objectCodec.readTree(jsonParser);

        JsonNode contentNode = node.get("$");

        if (contentNode == null) {
            return null;
        }

        return contentNode;
    }
}
