package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.serializer.BelfiusTransactionListDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonDeserialize(using = BelfiusTransactionListDeserializer.class)
public class BelfiusTransactionList {

    private List<BelfiusTransaction> transactions;

    public BelfiusTransactionList() {}

    public BelfiusTransactionList(List<BelfiusTransaction> transactions) {
        this.transactions = transactions;
    }

    public List<BelfiusTransaction> getTransactions() {
        return transactions != null ? transactions : Collections.emptyList();
    }
}
