package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.security.KeyPair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KeyPairDeserializer extends JsonDeserializer<KeyPair> {

    @Override
    public KeyPair deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String serializedKeyPair = p.getCodec().readTree(p).toString();
        return SerializationUtils.deserializeKeyPair(serializedKeyPair);
    }
}