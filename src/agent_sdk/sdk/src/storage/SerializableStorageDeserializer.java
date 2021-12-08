package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SerializableStorageDeserializer extends StdDeserializer<SerializableStorage> {

    public SerializableStorageDeserializer() {
        this(null);
    }

    public SerializableStorageDeserializer(Class<SerializableStorage> vc) {
        super(vc);
    }

    @Override
    public SerializableStorage deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException {
        HashMap<String, String> storage =
                parser.readValueAs(new TypeReference<Map<String, String>>() {});
        return new SerializableStorage(storage);
    }
}
