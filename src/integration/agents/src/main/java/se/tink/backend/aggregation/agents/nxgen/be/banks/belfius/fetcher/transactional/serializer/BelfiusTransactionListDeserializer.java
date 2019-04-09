package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransactionList;

public class BelfiusTransactionListDeserializer extends JsonDeserializer<BelfiusTransactionList> {

    @Override
    public BelfiusTransactionList deserialize(JsonParser jsonParser, DeserializationContext context)
            throws IOException {
        List<BelfiusTransaction> transactions = new ArrayList<>();

        JsonNode json = jsonParser.getCodec().readTree(jsonParser);
        json.get("contentList")
                .get("dynamiccontent")
                .elements()
                .forEachRemaining(entry -> deserializeInto(transactions, jsonParser, entry));

        return new BelfiusTransactionList(transactions);
    }

    public void deserializeInto(
            List<BelfiusTransaction> transactions, JsonParser jsonParser, JsonNode entry) {
        try {
            String key = entry.get("key").asText();
            JsonNode value = entry.get("rp_hist");
            BelfiusTransaction transaction =
                    jsonParser.getCodec().treeToValue(value, BelfiusTransaction.class);
            transactions.add(transaction);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
