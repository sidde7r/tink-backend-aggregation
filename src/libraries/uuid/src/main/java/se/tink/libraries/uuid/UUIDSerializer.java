package se.tink.libraries.uuid;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.UUID;

public class UUIDSerializer extends JsonSerializer<UUID> {

    @Override
    public void serialize(UUID uuid, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        String s = null;
        if (uuid == null) {
            jgen.writeString(s);
        }

        jgen.writeString(UUIDUtils.toTinkUUID(uuid));
    }
}
