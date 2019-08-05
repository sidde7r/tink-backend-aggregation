package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EmbeddedEntity {

    @JsonProperty("next_page_key")
    private String nextPageKey;

    private List<TransactionEntity> transactions;

    public String getNextPageKey() {
        return nextPageKey;
    }

    public void setNextPageKey(String nextPageKey) {
        this.nextPageKey = nextPageKey;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }
}
