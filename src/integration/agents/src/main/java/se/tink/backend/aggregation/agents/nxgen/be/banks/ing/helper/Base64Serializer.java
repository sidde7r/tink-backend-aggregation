package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Base64;

public class Base64Serializer extends StdSerializer<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Base64Serializer() {
        this(null);
    }

    protected Base64Serializer(Class<Object> t) {
        super(t);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        String encoded;
        if (value instanceof String) {
            encoded = Base64.getEncoder().encodeToString(((String) value).getBytes());
        } else {
            encoded = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(value));
        }
        gen.writeString(encoded);
    }
}
