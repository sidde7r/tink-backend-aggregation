package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.security.KeyPair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KeyPairSerializer extends JsonSerializer<KeyPair> {

    @Override
    public void serialize(
            KeyPair keyPair, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        String serializedKeyPair = SerializationUtils.serializeKeyPair(keyPair);
        jsonGenerator.writeRawValue(serializedKeyPair);
    }
}
