package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsResponse {
    private String accountId;
    private List<TransactionEntity> transactions;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @JsonProperty("AccountId")
    public void setAccountIdCaps(String accountId) {
        this.accountId = accountId;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions != null ? transactions : Lists.<TransactionEntity>newArrayList();
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    @JsonProperty("Transactions")
    public void setTransactionsCaps(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> toTinkTransactions() {
        List<Transaction> tinkTransactions = Lists.newArrayList();

        for (TransactionEntity transaction : getTransactions()) {
            tinkTransactions.add(transaction.toTinkTransaction());
        }

        return tinkTransactions;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("AccountId", accountId)
                .add("Transactions", transactions)
                .toString();
    }
}
