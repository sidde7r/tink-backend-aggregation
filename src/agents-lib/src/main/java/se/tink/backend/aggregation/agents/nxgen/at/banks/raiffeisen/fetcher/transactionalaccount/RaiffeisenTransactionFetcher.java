package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.RaiffeisenWebApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class RaiffeisenTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final RaiffeisenWebApiClient apiClient;
    private final RaiffeisenSessionStorage sessionStorage;

    public RaiffeisenTransactionFetcher(RaiffeisenWebApiClient apiClient, RaiffeisenSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        WebLoginResponse webLoginResponse = sessionStorage.getWebLoginResponse().orElseThrow(
                () -> new IllegalStateException("Failed to fetch login response when fetching transactions"));
        return apiClient.getTransactionsResponse(webLoginResponse, account.getAccountNumber(), fromDate, toDate);
    }
}
