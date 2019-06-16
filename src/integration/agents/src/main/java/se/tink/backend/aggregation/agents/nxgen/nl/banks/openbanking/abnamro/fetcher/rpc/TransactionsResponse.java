package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.fetcher.entities.TransactionListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponse {

    private String accountNumber;
    private String nextPageKey;
    private List<TransactionEntity> transactions;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getNextPageKey() {
        return nextPageKey;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public TransactionListEntity getTransactionList() {
        TransactionListEntity listEntity = new TransactionListEntity();
        listEntity.setTransactions(transactions);
        return listEntity;
    }
}
