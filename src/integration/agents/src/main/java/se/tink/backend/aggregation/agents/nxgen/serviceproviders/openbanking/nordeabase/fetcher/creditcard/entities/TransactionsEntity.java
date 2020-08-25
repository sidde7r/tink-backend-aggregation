package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {
    private List<TransactionEntity> transactions;

    @JsonProperty("continuation_key")
    private String continuationKey;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public String getContinuationKey() {
        return continuationKey;
    }
}
