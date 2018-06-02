package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import se.tink.backend.system.rpc.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse {
    @JsonProperty("GetTransactionsResult")
    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> toTransactions() {
        if (this.transactions == null) {
            return Collections.emptyList();
        }

        List<Transaction> transactions = Lists.newArrayList();
        for (TransactionEntity t : this.transactions) {
            transactions.add(t.toTransaction());
        }

        return transactions;
    }
}
