package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ErrorsEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class AxaErrorsDeserializer extends JsonDeserializer<List<ErrorsEntity>> {
    @Override
    public List<ErrorsEntity> deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final List<ErrorsEntity> entities = new ArrayList<>();

        if (node.isNull()) {
            return null;
        } else if (node.isArray()) {
            node.forEach(child -> entities.add(nodeToEntity(child)));
        } else if (node.isObject()) {
            entities.add(nodeToEntity(node));
        }

        return entities;
    }

    private static ErrorsEntity nodeToEntity(final JsonNode node) {
        return SerializationUtils.deserializeFromString(node.toString(), ErrorsEntity.class);
    }
}
