package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.ConsentParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.TransactionFetcherParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class VolksbankApiClient {

    private final SimpleDateFormat transactionDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final TinkHttpClient client;
    private final VolksbankUrlFactory urlFactory;

    public VolksbankApiClient(final TinkHttpClient client, final VolksbankUrlFactory urlFactory) {
        this.client = client;
        this.urlFactory = urlFactory;
    }

    public TransactionResponse readTransactionsWithDates(
            final TransactionalAccount account,
            final Date fromDate,
            final Date toDate,
            final String consentId,
            final OAuth2Token oauth2Token) {
        RequestBuilder request =
                client.request(getTransactionsUrl(account))
                        .queryParam(
                                TransactionFetcherParams.DATE_FROM,
                                transactionDateFormat.format(fromDate))
                        .queryParam(
                                TransactionFetcherParams.DATE_TO,
                                transactionDateFormat.format(toDate))
                        .queryParam(
                                TransactionFetcherParams.BOOKING_STATUS,
                                TransactionFetcherParams.BOOKING_STATUS_VALUE)
                        .queryParam(
                                TransactionFetcherParams.PAGE_DIRECTION,
                                TransactionFetcherParams.PAGE_DIRECTION_VALUE)
                        .queryParam(
                                TransactionFetcherParams.LIMIT,
                                TransactionFetcherParams.LIMIT_VALUE.toString());
        return readTransactions(request, consentId, oauth2Token);
    }

    public TransactionResponse readTransactionsWithLink(
            final TransactionalAccount account,
            final Map<String, String> urlParams,
            final String consentId,
            final OAuth2Token oauth2Token) {
        RequestBuilder request = client.request(getTransactionsUrl(account));
        for (String key : urlParams.keySet()) {
            request = request.queryParam(key, urlParams.get(key));
        }
        return readTransactions(request, consentId, oauth2Token);
    }

    private URL getTransactionsUrl(TransactionalAccount account) {
        return urlFactory.buildURL(
                Urls.HOST, Paths.ACCOUNTS + "/" + account.getApiIdentifier() + Paths.TRANSACTIONS);
    }

    private TransactionResponse readTransactions(
            RequestBuilder request, final String consentId, final OAuth2Token oauth2Token) {

        request.header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.CONSENT_ID, consentId)
                .addBearerToken(oauth2Token);

        return request.get(TransactionResponse.class);
    }

    public BalancesResponse readBalance(
            AccountsEntity account, final String consentId, final OAuth2Token oAuth2Token) {

        URL url =
                urlFactory.buildURL(
                        Urls.HOST, Paths.ACCOUNTS + "/" + account.getResourceId() + Paths.BALANCES);

        return client.request(url)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.CONSENT_ID, consentId)
                .addBearerToken(oAuth2Token)
                .get(BalancesResponse.class);
    }

    public AccountsResponse fetchAccounts(final String consentId, final OAuth2Token oAuth2Token) {

        URL url = urlFactory.buildURL(Urls.HOST, Paths.ACCOUNTS);

        return client.request(url)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.CONSENT_ID, consentId)
                .addBearerToken(oAuth2Token)
                .get(AccountsResponse.class);
    }

    public ConsentResponse consentRequest(final URL redirectUrl, final String clientId) {
        final URL url = urlFactory.buildURL(Urls.HOST, VolksbankConstants.Paths.CONSENT);
        final ConsentRequestBody body =
                new ConsentRequestBody(
                        VolksbankUtils.getFutureDateAsString(ConsentParams.VALID_YEAR),
                        ConsentParams.FREQUENCY_PER_DAY,
                        ConsentParams.RECURRING_INDICATOR);

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.AUTHORIZATION, clientId)
                .body(body)
                .post(ConsentResponse.class);
    }

    public OAuth2Token getBearerToken(
            final URL url, final String clientId, final String clientSecret) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.REQUEST_ID, Psd2Headers.getRequestId())
                .addBasicAuth(clientId, clientSecret)
                .post(TokenResponse.class)
                .toOauthToken();
    }

    public ConsentStatusResponse consentStatusRequest(
            final String clientId, final String consentId) {
        final URL url = urlFactory.buildURL(Urls.HOST, Paths.CONSENT + "/" + consentId + "/status");

        return client.request(url)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE)
                .header(HeaderKeys.REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.AUTHORIZATION, clientId)
                .get(ConsentStatusResponse.class);
    }
}
