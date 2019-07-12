package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class UnicreditTransactionalAccountTransactionFetcher
        implements TransactionFetcher<TransactionalAccount> {

    private final UnicreditBaseApiClient apiClient;

    public UnicreditTransactionalAccountTransactionFetcher(UnicreditBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return new ArrayList<>(apiClient.getTransactionsFor(account).getTinkTransactions());
    }
}
