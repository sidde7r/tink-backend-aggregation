package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.serializer.BelfiusUpcomingTransactionListDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Collections;
import java.util.List;

@JsonObject
@JsonDeserialize(using = BelfiusUpcomingTransactionListDeserializer.class)
public class BelfiusUpcomingTransactionList {

    private List<BelfiusUpcomingTransaction> transactions;

    public BelfiusUpcomingTransactionList() {
    }

    public BelfiusUpcomingTransactionList(
            List<BelfiusUpcomingTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<BelfiusUpcomingTransaction> getTransactions() {
        return transactions != null ? transactions : Collections.emptyList();
    }
}
