package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CreditAgricoleTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionFetcher<TransactionalAccount> {

    private final CreditAgricoleApiClient apiClient;

    public CreditAgricoleTransactionalAccountFetcher(CreditAgricoleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().toTinkAccounts();
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        return apiClient.getTransactions(account.getApiIdentifier()).toTinkTransactions();
    }
}
