package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Urls.API_PSD2_URL;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

@RequiredArgsConstructor
public class FabricFetcherApiClient {

    private final FabricRequestBuilder requestBuilder;
    private final String baseUrl;

    public AccountResponse fetchAccounts(String consentId) {
        return requestBuilder
                .createRequestInSession(new URL(baseUrl + Urls.GET_ACCOUNTS), consentId)
                .get(AccountResponse.class);
    }

    public BalanceResponse getBalances(String consentId, String url) {
        return requestBuilder
                .createRequestInSession(new URL(baseUrl + API_PSD2_URL + url), consentId)
                .get(BalanceResponse.class);
    }

    public AccountDetailsResponse getAccountDetails(String consentId, String url) {
        return requestBuilder
                .createRequestInSession(new URL(baseUrl + API_PSD2_URL + url), consentId)
                .get(AccountDetailsResponse.class);
    }

    public TransactionResponse fetchTransactions(
            String consentId, String resourceId, Date fromDate, Date toDate) {
        return requestBuilder
                .createRequestInSession(
                        new URL(baseUrl + Urls.GET_TRANSACTIONS)
                                .parameter(IdTags.ACCOUNT_ID, resourceId),
                        consentId)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(TransactionResponse.class);
    }
}
