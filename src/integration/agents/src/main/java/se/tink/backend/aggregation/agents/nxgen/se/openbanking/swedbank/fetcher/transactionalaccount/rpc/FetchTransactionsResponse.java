package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTransactionsResponse {
    private AccountEntity account;
    private TransactionsEntity transactions;

    public AccountEntity getAccount() {
        return account;
    }

    public TransactionsEntity getTransactions() {
        return transactions;
    }
}
