package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import java.io.IOException;
import java.util.Base64;

public class Base64Deserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private JavaType type;

    public Base64Deserializer() {}

    public Base64Deserializer(JavaType type) {
        this.type = type;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        String encoded = p.getValueAsString();
        byte[] decode = Base64.getDecoder().decode(encoded);

        return objectMapper.readValue(decode, type);
    }

    @Override
    public JsonDeserializer<?> createContextual(
            DeserializationContext ctxt, BeanProperty property) {
        JavaType determinedType =
                ctxt.getContextualType() != null
                        ? ctxt.getContextualType()
                        : property.getMember().getType();
        return new Base64Deserializer(determinedType);
    }
}
