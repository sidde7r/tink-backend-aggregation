package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DemobankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {
    private final DemobankApiClient apiClient;
    private final Provider provider;

    public DemobankTransactionalAccountFetcher(DemobankApiClient apiClient, Provider provider) {
        this.apiClient = apiClient;
        this.provider = provider;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().stream()
                .filter(AccountEntity::isNotCreditCard)
                .map(
                        accountEntity ->
                                accountEntity.toTinkAccount(
                                        apiClient.fetchAccountHolders(accountEntity.getId())))
                .filter(
                        a ->
                                AccountFetcherUtils.getHolderTypeSupportedByProvider(provider)
                                        == a.getHolderType())
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchTransactions(account.getApiIdentifier(), fromDate, toDate);
    }
}
