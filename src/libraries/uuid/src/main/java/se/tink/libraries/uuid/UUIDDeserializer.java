package se.tink.libraries.uuid;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.UUID;

public class UUIDDeserializer extends JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String value = jp.getValueAsString();
        if (value == null) {
            return null;
        }

        return UUIDUtils.fromTinkUUID(value.replace("-", ""));
    }
}
