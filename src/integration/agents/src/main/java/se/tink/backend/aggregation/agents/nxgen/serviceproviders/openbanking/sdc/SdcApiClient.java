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
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.RefreshAccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SdcApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private SdcConfiguration configuration;
    private String redirectUrl;

    public SdcApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, Credentials credentials) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
    }

    private SdcConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(
            AgentConfiguration<SdcConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.client.setEidasProxy(eidasProxyConfiguration);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL buildAuthorizeUrl(String state) {
        return createRequest(Urls.AUTHORIZATION)
                .queryParam(QueryKeys.SCOPE, HeaderValues.SCOPE_AIS)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.REDIRECT_URI, getRedirectUrl())
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token exchangeAuthorizationCode(String code) {

        TokenRequest tokenRequest =
                new TokenRequest(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        FormValues.SCOPE_AIS,
                        credentials.getField(Key.LOGIN_INPUT),
                        "");

        return createRequest(Urls.TOKEN)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    public OAuth2Token refreshAccessToken(String refreshToken) {

        RefreshAccessTokenRequest tokenRequest =
                new RefreshAccessTokenRequest(
                        FormValues.REFRESH_TOKEN,
                        refreshToken,
                        getRedirectUrl(),
                        getConfiguration().getClientId(),
                        getConfiguration().getClientSecret(),
                        FormValues.SCOPE_AIS);

        return createRequest(Urls.TOKEN)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(Urls.ACCOUNTS)
                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(CONSENT_ID, Psd2Headers.getRequestId())
                .header(OCP_APIM_SUBSCRIPTION_KEY, getConfiguration().getOcpApimSubscriptionKey())
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchAccountBalances(String accountId) {
        return createRequestInSession(Urls.BALANCES.parameter(ACCOUNT_ID, accountId))
                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(CONSENT_ID, Psd2Headers.getRequestId())
                .header(OCP_APIM_SUBSCRIPTION_KEY, getConfiguration().getOcpApimSubscriptionKey())
                .get(BalancesResponse.class);
    }

    public TransactionsResponse getTransactionsFor(String accountId, Date fromDate, Date toDate) {
        return createRequestInSession(Urls.TRANSACTIONS.parameter(ACCOUNT_ID, accountId))
                .header(X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(CONSENT_ID, Psd2Headers.getRequestId())
                .header(OCP_APIM_SUBSCRIPTION_KEY, getConfiguration().getOcpApimSubscriptionKey())
                .queryParam(BOOKING_STATUS, BOOKED)
                .queryParam(DATE_FROM, FORMATTER_DAILY.format(fromDate))
                .queryParam(DATE_TO, FORMATTER_DAILY.format(toDate))
                .get(TransactionsResponse.class);
    }
}
