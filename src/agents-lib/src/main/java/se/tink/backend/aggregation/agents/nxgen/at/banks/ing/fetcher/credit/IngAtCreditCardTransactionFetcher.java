package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.credit;

import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

import java.util.Date;
import java.util.Optional;

public class IngAtCreditCardTransactionFetcher
        implements TransactionDatePaginator<CreditCardAccount> {
    private final IngAtApiClient apiClient;
    private final IngAtSessionStorage sessionStorage;

    public IngAtCreditCardTransactionFetcher(
            IngAtApiClient apiClient, IngAtSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account,
            Date fromDate,
            Date toDate) {
        final Optional<WebLoginResponse> webLoginResponse = sessionStorage.getWebLoginResponse();
        webLoginResponse.orElseThrow(
                () ->
                        new IllegalStateException(
                                "Failed to fetch login response when fetching transactions"));
        return apiClient.getTransactionsResponse(account);
    }
}
