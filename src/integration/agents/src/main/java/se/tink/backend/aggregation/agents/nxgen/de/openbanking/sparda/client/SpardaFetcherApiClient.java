package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.Urls.ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.Urls.TRANSACTIONS;

import java.time.LocalDate;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class SpardaFetcherApiClient {

    private static final String ACCOUNT_ID = "accountId";

    private final SpardaRequestBuilder requestBuilder;
    private final SpardaErrorHandler errorHandler;

    public FetchAccountsResponse fetchAccounts() {
        return sendRequest(
                () ->
                        requestBuilder
                                .createRequestWithConsent(ACCOUNTS)
                                .get(FetchAccountsResponse.class));
    }

    public FetchBalancesResponse fetchBalances(String url) {
        return sendRequest(
                () ->
                        requestBuilder
                                .createRequestWithConsent(new URL(url))
                                .get(FetchBalancesResponse.class));
    }

    public FetchTransactionsResponse fetchTransactions(String accountId, LocalDate startDate) {
        return sendRequest(
                () ->
                        requestBuilder
                                .createRequestWithConsent(
                                        TRANSACTIONS
                                                .parameter(ACCOUNT_ID, accountId)
                                                .queryParam("bookingStatus", "both")
                                                .queryParam("dateFrom", startDate.toString()))
                                .get(FetchTransactionsResponse.class));
    }

    public FetchTransactionsResponse fetchTransactions(String nextUrl) {
        return sendRequest(
                () ->
                        requestBuilder
                                .createRequestWithConsent(new URL(nextUrl))
                                .get(FetchTransactionsResponse.class));
    }

    @SneakyThrows
    private <T> T sendRequest(Callable<T> requestCallable) {
        try {
            return requestCallable.call();
        } catch (HttpResponseException e) {
            errorHandler.tryHandleTokenExpiredError(e);
            return requestCallable.call();
        }
    }
}
