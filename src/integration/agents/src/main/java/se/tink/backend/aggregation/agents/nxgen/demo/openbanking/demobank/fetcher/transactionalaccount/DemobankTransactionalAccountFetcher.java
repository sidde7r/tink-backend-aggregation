package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount;

import io.vavr.control.Option;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DemobankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, String> {
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
                                AccountFetcherUtils.inferHolderTypeFromProvider(provider)
                                        == a.getHolderType())
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String nextPageToken) {
        final String accountNumber = account.getApiIdentifier();

        return Option.of(nextPageToken)
                .fold(
                        () -> apiClient.fetchTransactions(accountNumber),
                        s -> apiClient.fetchTransactions(accountNumber, s));
    }
}
