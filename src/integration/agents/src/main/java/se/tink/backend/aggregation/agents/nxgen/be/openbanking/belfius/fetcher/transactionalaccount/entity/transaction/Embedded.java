package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Embedded {

    @JsonProperty("next_page_key")
    private String nextPageKey;

    private List<Transaction> transactions;

    public String getNextPageKey() {
        return nextPageKey;
    }

    public void setNextPageKey(String nextPageKey) {
        this.nextPageKey = nextPageKey;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
