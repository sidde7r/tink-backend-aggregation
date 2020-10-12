package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity.UnavailabilityEntity;

public class UnavailabilityDeserializer extends JsonDeserializer<UnavailabilityEntity> {

    @Override
    public UnavailabilityEntity deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {

        JsonNode node = p.readValueAsTree();

        if (node.isObject()) {
            UnavailabilityEntity entity =
                    new ObjectMapper().readValue(node.toString(), UnavailabilityEntity.class);
            entity.setDown("PLANNED".equals(entity.getType()));
            return entity;
        } else if (node.isBoolean()) {
            return new UnavailabilityEntity(node.asBoolean());
        }

        return new UnavailabilityEntity(false);
    }
}
