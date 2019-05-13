package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Transactions {

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("nextPageKey")
    private String nextPageKey;

    @JsonProperty("transactions")
    private List<TransactionItem> transactions;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getNextPageKey() {
        return nextPageKey;
    }

    public void setNextPageKey(String nextPageKey) {
        this.nextPageKey = nextPageKey;
    }

    public List<TransactionItem> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionItem> transactions) {
        this.transactions = transactions;
    }
}
