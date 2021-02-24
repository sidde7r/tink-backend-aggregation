package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.HeaderKeys.OCP_APIM_SUBSCRIPTION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.PathParameters.ACCOUNT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys.BOOKING_STATUS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys.DATE_FROM;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys.DATE_TO;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryValues.BOOKED;
import static se.tink.backend.aggregation.api.Psd2Headers.Keys.CONSENT_ID;
import static se.tink.backend.aggregation.api.Psd2Headers.Keys.X_REQUEST_ID;
import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.OAuth2TokenEnricher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.RefreshAccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.filters.BankInternalErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.filters.BankInternalErrorRetryFilter;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SdcApiClient {

    private final TinkHttpClient client;
    private final SdcUrlProvider urlProvider;
    private final PersistentStorage persistentStorage;
    private final SdcConfiguration configuration;
    private final String redirectUrl;

    public SdcApiClient(
            TinkHttpClient client,
            SdcUrlProvider urlProvider,
            PersistentStorage persistentStorage,
            SdcConfiguration configuration,
            String redirectUrl) {
        this.client = client;
        this.urlProvider = urlProvider;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.redirectUrl = redirectUrl;

        this.client.addFilter(new BankInternalErrorFilter());
        this.client.addFilter(new BankInternalErrorRetryFilter());
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();
        OAuth2TokenEnricher.enrich(authToken);
        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL buildAuthorizeUrl(String state) {
        return createRequest(urlProvider.getAuthorizationUrl())
                .queryParam(QueryKeys.SCOPE, HeaderValues.SCOPE_AIS)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {
        TokenRequest tokenRequest =
                new TokenRequest(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        redirectUrl,
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        FormValues.SCOPE_AIS,
                        null,
                        "");

        return createRequest(urlProvider.getTokenUrl())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    public OAuth2Token refreshAccessToken(String refreshToken) {
        RefreshAccessTokenRequest tokenRequest =
                new RefreshAccessTokenRequest(
                        FormValues.REFRESH_TOKEN,
                        refreshToken,
                        redirectUrl,
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        FormValues.SCOPE_AIS);

        return createRequest(urlProvider.getTokenUrl())
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(urlProvider.getAccountsUrl())
                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(CONSENT_ID, Psd2Headers.getRequestId())
                .header(OCP_APIM_SUBSCRIPTION_KEY, configuration.getOcpApimSubscriptionKey())
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchAccountBalances(String accountId) {
        return createRequestInSession(urlProvider.getBalancesUrl().parameter(ACCOUNT_ID, accountId))
                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(CONSENT_ID, Psd2Headers.getRequestId())
                .header(OCP_APIM_SUBSCRIPTION_KEY, configuration.getOcpApimSubscriptionKey())
                .get(BalancesResponse.class);
    }

    public TransactionsResponse getTransactionsFor(String accountId, Date fromDate, Date toDate) {
        return createRequestInSession(
                        urlProvider.getTransactionsUrl().parameter(ACCOUNT_ID, accountId))
                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(CONSENT_ID, Psd2Headers.getRequestId())
                .header(OCP_APIM_SUBSCRIPTION_KEY, configuration.getOcpApimSubscriptionKey())
                .queryParam(BOOKING_STATUS, BOOKED)
                .queryParam(DATE_FROM, FORMATTER_DAILY.format(fromDate))
                .queryParam(DATE_TO, FORMATTER_DAILY.format(toDate))
                .get(TransactionsResponse.class);
    }
}
