package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.ErrorsEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AxaDeserializer extends JsonDeserializer<List<ErrorsEntity>> {
    @Override
    public List<ErrorsEntity> deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final List<ErrorsEntity> errors = new ArrayList<>();

        if (node.isNull()) {
            return null;
        } else if (node.isArray()) {
            node.forEach(child -> errors.add(nodeToEntity(child)));
        } else if (node.isObject()) {
            errors.add(nodeToEntity(node));
        }

        return errors;
    }

    private static ErrorsEntity nodeToEntity(final JsonNode node) {
        return SerializationUtils.deserializeFromString(node.toString(), ErrorsEntity.class);
    }
}
