package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AxaAccountsDeserializer extends JsonDeserializer<List<AccountEntity>> {
    @Override
    public List<AccountEntity> deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final List<AccountEntity> entities = new ArrayList<>();

        if (node.isNull()) {
            return null;
        } else if (node.isArray()) {
            node.forEach(child -> entities.add(nodeToEntity(child)));
        } else if (node.isObject()) {
            entities.add(nodeToEntity(node));
        }

        return entities;
    }

    private static AccountEntity nodeToEntity(final JsonNode node) {
        return SerializationUtils.deserializeFromString(node.toString(), AccountEntity.class);
    }
}
