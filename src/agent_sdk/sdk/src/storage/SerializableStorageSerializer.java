package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class SerializableStorageSerializer extends StdSerializer<SerializableStorage> {
    public SerializableStorageSerializer() {
        this(null);
    }

    public SerializableStorageSerializer(Class<SerializableStorage> t) {
        super(t);
    }

    @Override
    public void serialize(SerializableStorage value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeObject(value.storage);
    }
}
