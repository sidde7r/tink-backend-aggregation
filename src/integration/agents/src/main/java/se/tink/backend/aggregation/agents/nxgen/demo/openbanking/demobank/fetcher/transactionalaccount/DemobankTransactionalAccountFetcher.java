package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemobankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionKeyPaginator {
    private final DemobankApiClient apiClient;
    private final SessionStorage sessionStorage;

    public DemobankTransactionalAccountFetcher(
            DemobankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> accounts = Lists.newArrayList();
        FetchAccountResponse accountResponse = apiClient.fetchAccounts();
        accountResponse.stream()
                .map(AccountEntity::toTinkAccount)
                .forEach(account -> accounts.add(account));

        return accounts;
    }

    @Override
    public TransactionKeyPaginatorResponse getTransactionsFor(Account account, Object key) {
        return apiClient.fetchTransactions(account.getApiIdentifier());
    }
}
