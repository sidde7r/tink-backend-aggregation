package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class ConsorsbankFetcherApiClient {

    private static final URL ACCOUNTS_ENDPOINT = new URL("https://xs2a.consorsbank.de/v1/accounts");
    private static final URL TRANSACTIONS_ENDPOINT =
            new URL("https://xs2a.consorsbank.de/v1/accounts/{accountId}/transactions");

    private static final String ACCOUNT_ID = "accountId";

    private final ConsorsbankRequestBuilder requestBuilder;

    public FetchAccountsResponse fetchAccounts(String consentId, boolean shouldFetchWithBalances) {
        return requestBuilder
                .createRequestInSession(
                        ACCOUNTS_ENDPOINT.queryParam(
                                "withBalance", String.valueOf(shouldFetchWithBalances)),
                        consentId)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(String consentId, String url) {
        return requestBuilder
                .createRequestInSession(new URL(url), consentId)
                .get(FetchBalancesResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            String consentId, String accountId, LocalDate startDate) {
        return requestBuilder
                .createRequestInSession(
                        TRANSACTIONS_ENDPOINT
                                .parameter(ACCOUNT_ID, accountId)
                                .queryParam("bookingStatus", "both")
                                .queryParam("dateFrom", startDate.toString()),
                        consentId)
                .get(FetchTransactionsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(String consentId, String nextUrl) {
        return requestBuilder
                .createRequestInSession(new URL(nextUrl), consentId)
                .get(FetchTransactionsResponse.class);
    }
}
