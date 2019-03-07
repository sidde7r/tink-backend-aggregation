package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountTransactionsEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.io.IOException;

public final class AxaAccountTransactionsEntityDeserializer
        extends JsonDeserializer<AccountTransactionsEntity> {
    @Override
    public AccountTransactionsEntity deserialize(
            JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (node.isNull()) {
            return null;
        } else if (node.isTextual() && node.textValue().isEmpty()) {
            return AccountTransactionsEntity.createEmpty();
        } else if (node.isObject()) {
            return SerializationUtils.deserializeFromString(
                    node.toString(), AccountTransactionsEntity.class);
        } else {
            throw new IllegalStateException();
        }
    }
}
