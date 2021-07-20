package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.HeaderKeys.OCP_APIM_SUBSCRIPTION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.PathParameters.ACCOUNT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys.BOOKING_STATUS;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys.DATE_FROM;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys.DATE_TO;
import static se.tink.backend.aggregation.api.Psd2Headers.Keys.CONSENT_ID;
import static se.tink.backend.aggregation.api.Psd2Headers.Keys.X_REQUEST_ID;
import static se.tink.libraries.date.ThreadSafeDateFormat.FORMATTER_DAILY;

import java.util.Date;
import java.util.concurrent.Callable;
import javax.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.ErrorMessages;
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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
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

    public URL buildAuthorizeUrl(String state) {
        return createRequest(urlProvider.getAuthorizationUrl())
                .queryParam(QueryKeys.SCOPE, HeaderValues.SCOPE_AIS)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.LOGIN_TYPE, QueryValues.NEMID_BANK_LOGIN)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {
        return postForAccessToken(
                new TokenRequest(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        redirectUrl,
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        FormValues.SCOPE_AIS,
                        null,
                        ""));
    }

    public void refreshAccessToken() {
        OAuth2Token accessToken =
                postForAccessToken(
                        new RefreshAccessTokenRequest(
                                FormValues.REFRESH_TOKEN,
                                getRefreshTokenFromStorage(),
                                redirectUrl,
                                configuration.getClientId(),
                                configuration.getClientSecret(),
                                FormValues.SCOPE_AIS));
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private <T> OAuth2Token postForAccessToken(T tokenRequest) {
        OAuth2Token accessToken =
                createRequest(urlProvider.getTokenUrl())
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .post(TokenResponse.class, tokenRequest)
                        .toTinkToken();

        // SDC provides Oauth2 Token without information about token type
        OAuth2TokenEnricher.enrich(accessToken);

        return accessToken;
    }

    public AccountsResponse fetchAccounts() {
        return sendSessionRequest(
                () ->
                        createRequestInSession(urlProvider.getAccountsUrl())
                                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                                .header(CONSENT_ID, Psd2Headers.getRequestId())
                                .header(
                                        OCP_APIM_SUBSCRIPTION_KEY,
                                        configuration.getOcpApimSubscriptionKey())
                                .get(AccountsResponse.class));
    }

    public BalancesResponse fetchAccountBalances(String accountId) {
        return sendSessionRequest(
                () ->
                        createRequestInSession(
                                        urlProvider
                                                .getBalancesUrl()
                                                .parameter(ACCOUNT_ID, accountId))
                                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                                .header(CONSENT_ID, Psd2Headers.getRequestId())
                                .header(
                                        OCP_APIM_SUBSCRIPTION_KEY,
                                        configuration.getOcpApimSubscriptionKey())
                                .get(BalancesResponse.class));
    }

    public TransactionsResponse getTransactionsFor(
            String accountId,
            Date fromDate,
            Date toDate,
            String providerMarket,
            String bookingStatus) {
        return sendSessionRequest(
                () ->
                        createRequestInSession(
                                        urlProvider
                                                .getTransactionsUrl()
                                                .parameter(ACCOUNT_ID, accountId))
                                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                                .header(CONSENT_ID, Psd2Headers.getRequestId())
                                .header(
                                        OCP_APIM_SUBSCRIPTION_KEY,
                                        configuration.getOcpApimSubscriptionKey())
                                .queryParam(BOOKING_STATUS, bookingStatus)
                                .queryParam(DATE_FROM, FORMATTER_DAILY.format(fromDate))
                                .queryParam(DATE_TO, FORMATTER_DAILY.format(toDate))
                                .get(TransactionsResponse.class)
                                .setProviderMarket(providerMarket));
    }

    private RequestBuilder createRequestInSession(URL url) {
        OAuth2Token authToken = getTokenFromStorage();
        return createRequest(url).addBearerToken(authToken);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private String getRefreshTokenFromStorage() {
        return getTokenFromStorage()
                .getRefreshToken()
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    @SneakyThrows
    private <T> T sendSessionRequest(Callable<T> requestCallable) {
        try {
            return requestCallable.call();

        } catch (HttpResponseException e) {
            /*
            Sometimes fetching data (mainly transactions) takes so long than current access token expires
             */
            if (e.getMessage().contains(ErrorMessages.TOKEN_EXPIRED)) {
                log.info("Refreshing access token to continue fetching data");
                refreshAccessToken();
                return requestCallable.call();
            }
            throw e;
        }
    }
}
