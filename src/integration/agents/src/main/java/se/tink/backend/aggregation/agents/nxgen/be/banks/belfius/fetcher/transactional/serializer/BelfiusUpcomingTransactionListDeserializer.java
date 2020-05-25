package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusUpcomingTransaction;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusUpcomingTransactionList;

public class BelfiusUpcomingTransactionListDeserializer
        extends JsonDeserializer<BelfiusUpcomingTransactionList> {

    @Override
    public BelfiusUpcomingTransactionList deserialize(
            JsonParser jsonParser, DeserializationContext context) throws IOException {
        List<BelfiusUpcomingTransaction> transactions = new ArrayList<>();

        JsonNode json = jsonParser.getCodec().readTree(jsonParser);
        json.get("contentList")
                .get("dynamiccontent")
                .elements()
                .forEachRemaining(entry -> deserializeInto(transactions, jsonParser, entry));

        return new BelfiusUpcomingTransactionList(transactions);
    }

    public void deserializeInto(
            List<BelfiusUpcomingTransaction> transactions, JsonParser jsonParser, JsonNode entry) {
        try {
            JsonNode value = entry.get("rp_hist");
            BelfiusUpcomingTransaction transaction =
                    jsonParser.getCodec().treeToValue(value, BelfiusUpcomingTransaction.class);
            transactions.add(transaction);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
