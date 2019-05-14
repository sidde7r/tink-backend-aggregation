package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities.TransactionListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse {

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("nextPageKey")
    private String nextPageKey;

    @JsonProperty("transactions")
    private List<TransactionEntity> transactions;

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

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    @JsonIgnore private TransactionListEntity transactionList;

    public TransactionListEntity getTransactionList() {
        TransactionListEntity listEntity = new TransactionListEntity();
        listEntity.setTransactions(transactions);
        return listEntity;
    }
}
