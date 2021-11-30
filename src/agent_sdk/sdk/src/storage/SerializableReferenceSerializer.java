package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class SerializableReferenceSerializer extends StdSerializer<SerializableReference> {
    public SerializableReferenceSerializer() {
        this(null);
    }

    public SerializableReferenceSerializer(Class<SerializableReference> t) {
        super(t);
    }

    @Override
    public void serialize(
            SerializableReference value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeObject(value.storage);
    }
}
