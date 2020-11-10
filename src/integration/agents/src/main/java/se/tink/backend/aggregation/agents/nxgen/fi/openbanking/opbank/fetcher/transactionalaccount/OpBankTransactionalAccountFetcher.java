package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class OpBankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, URL> {

    private final OpBankApiClient apiClient;

    public OpBankTransactionalAccountFetcher(OpBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccountsList().stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL nextUrl) {
        URL url =
                Optional.ofNullable(nextUrl)
                        .orElse(
                                new URL(Urls.GET_TRANSACTIONS)
                                        .parameter(
                                                IdTags.ACCOUNT_ID,
                                                account.getFromTemporaryStorage(
                                                        StorageKeys.ACCOUNT_ID)));

        return apiClient.getTransactions(url);
    }
}
