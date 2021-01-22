package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.OnlineTransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchOnlineTransactionsResponse {
    private AccountEntity account;
    private OnlineTransactionsEntity transactions;

    public AccountEntity getAccount() {
        return account;
    }

    public OnlineTransactionsEntity getTransactions() {
        return transactions;
    }
}
