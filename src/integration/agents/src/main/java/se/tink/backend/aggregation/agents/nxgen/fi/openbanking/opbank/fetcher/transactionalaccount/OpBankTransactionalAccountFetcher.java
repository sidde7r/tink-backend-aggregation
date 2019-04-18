package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

import java.util.Collection;
import java.util.stream.Collectors;

public class OpBankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, URL> {

    private final OpBankApiClient apiClient;

    public OpBankTransactionalAccountFetcher(OpBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().stream()
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL nextUrl) {

        return apiClient.getTransactions(
                        account.getFromTemporaryStorage(OpBankConstants.StorageKeys.ACCOUNT_ID));
    }
}
