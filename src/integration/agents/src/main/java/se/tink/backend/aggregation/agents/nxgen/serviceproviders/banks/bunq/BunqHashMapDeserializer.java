package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BunqHashMapDeserializer {
    public static class BunqDeserializer extends JsonDeserializer<BunqResponse<?>>
            implements ContextualDeserializer {
        private static final ObjectMapper mapper = new ObjectMapper();
        private JavaType valueType;

        @Override
        public JsonDeserializer<?> createContextual(
                DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            JavaType wrapperType = property.getType();
            JavaType valueType = wrapperType.containedType(0);
            BunqDeserializer deserializer = new BunqDeserializer();
            deserializer.valueType = valueType;
            return deserializer;
        }

        @Override
        public BunqResponse<?> deserialize(JsonParser jsonParser, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            BunqResponse<?> bunqResponse = new BunqResponse<>();
            Field[] declaredFields = valueType.getRawClass().getDeclaredFields();

            List<String> names = Lists.newArrayList();
            for (Field field : declaredFields) {
                names.add(field.getAnnotation(JsonProperty.class).value());
            }

            ObjectCodec codec = jsonParser.getCodec();
            JsonNode node = codec.readTree(jsonParser);

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("{");
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                stringBuilder
                        .append("\"")
                        .append(name)
                        .append("\"")
                        .append(":")
                        .append(
                                StreamSupport.stream(node.spliterator(), false)
                                        .map(t -> t.get(name))
                                        .filter(Objects::nonNull)
                                        .map(JsonNode::toString)
                                        .collect(Collectors.joining()));

                if (i >= names.size() - 1) {
                    break;
                }

                stringBuilder.append(",");
            }
            stringBuilder.append("}");

            bunqResponse.setResponseBody(mapper.readValue(stringBuilder.toString(), valueType));
            return bunqResponse;
        }
    }
}
