package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class TransactionsResponse {
    private AccountDetailsResponse account;
    private List<Object> lockedEvents;
    private List<TransactionEntity> transactions;
    private Object continuationKey;

    public AccountDetailsResponse getAccount() {
        return account;
    }

    public List<Object> getLockedEvents() {
        return lockedEvents;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public Object getContinuationKey() {
        return continuationKey;
    }
}
