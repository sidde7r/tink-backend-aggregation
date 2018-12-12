package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional;

import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class IngAtTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {
    private final IngAtApiClient apiClient;
    private final IngAtSessionStorage sessionStorage;

    public IngAtTransactionFetcher(IngAtApiClient apiClient, IngAtSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, Date fromDate, Date toDate) {
        final Optional<WebLoginResponse> webLoginResponse = sessionStorage.getWebLoginResponse();
        webLoginResponse.orElseThrow(() -> new IllegalStateException("Failed to fetch login response when fetching transactions"));
        return apiClient.getTransactionsResponse(webLoginResponse.get(), account, fromDate, toDate);
    }
}