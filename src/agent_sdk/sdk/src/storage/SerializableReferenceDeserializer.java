package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class SerializableReferenceDeserializer extends StdDeserializer<SerializableReference> {

    public SerializableReferenceDeserializer() {
        this(null);
    }

    public SerializableReferenceDeserializer(Class<SerializableReference> vc) {
        super(vc);
    }

    @Override
    public SerializableReference deserialize(JsonParser parser, DeserializationContext ctxt)
            throws IOException {
        SerializableStorage storage = parser.readValueAs(SerializableStorage.class);
        return new SerializableReference(storage);
    }
}
