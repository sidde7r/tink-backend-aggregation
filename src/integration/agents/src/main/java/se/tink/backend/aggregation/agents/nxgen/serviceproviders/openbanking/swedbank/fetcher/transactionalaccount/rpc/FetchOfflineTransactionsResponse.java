package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.OfflineTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchOfflineTransactionsResponse {
    private AccountEntity account;
    private List<OfflineTransactionEntity> transactions;

    public AccountEntity getAccount() {
        return account;
    }

    public List<OfflineTransactionEntity> getTransactions() {
        return transactions;
    }
}
