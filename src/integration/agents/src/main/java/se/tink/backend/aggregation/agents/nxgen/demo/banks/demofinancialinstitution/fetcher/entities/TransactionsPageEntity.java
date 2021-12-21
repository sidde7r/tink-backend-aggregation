package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionsPageEntity {

    @JsonProperty("transactions")
    private List<TransactionEntity> transactions;

    @JsonProperty("nextPageToken")
    private String nextPageToken;

    @JsonIgnore
    public List<Transaction> toTinkTransactions() {
        return this.transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    @JsonIgnore
    public Boolean isNextPageToken() {
        return !Strings.isNullOrEmpty(nextPageToken);
    }
}
