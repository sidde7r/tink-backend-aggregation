package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import java.util.Arrays;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.OIDCValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.AccessConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.AccessConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.RequestDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.accessconsents.RiskEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.IdTokenClaim;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JwtAuthPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.InitialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction.TransactionTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyTransactionsResponse;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.JwtHeader;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.JwtUtils;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CrosskeyBaseApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private CrosskeyBaseConfiguration configuration;
    private EidasProxyConfiguration eidasProxyConfiguration;

    public CrosskeyBaseApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public CrosskeyBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(
            CrosskeyBaseConfiguration configuration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = configuration;
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.client.setEidasProxy(eidasProxyConfiguration, "Tink");
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String clientSecret = getConfiguration().getClientSecret();
        final String xFapiFinancialId = getConfiguration().getXFapiFinancialId();

        return createRequest(url)
                .addBearerToken(getTokenFromSession())
                .header(HeaderKeys.X_API_KEY, clientSecret)
                .header(HeaderKeys.X_FAPI_FINANCIAL_ID, xFapiFinancialId);
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequestInSession(new URL(url));
    }

    private RequestBuilder createAuthorizationRequest(
            InitialTokenResponse clientCredentials, URL url) {
        final String clientSecret = getConfiguration().getClientSecret();
        final String xFapiFinancialId = getConfiguration().getXFapiFinancialId();

        return createRequest(url)
                .addBearerToken(clientCredentials.toTinkToken())
                .header(HeaderKeys.X_API_KEY, clientSecret)
                .header(HeaderKeys.X_FAPI_FINANCIAL_ID, xFapiFinancialId);
    }

    private RequestBuilder createAuthorizationRequest(
            InitialTokenResponse clientCredentials, String url) {
        return createAuthorizationRequest(clientCredentials, new URL(url));
    }

    private RequestBuilder createTokenRequest(URL url) {
        final String clientId = getConfiguration().getClientId();

        return client.request(url)
                .header(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(QueryKeys.CLIENT_ID, clientId);
    }

    private RequestBuilder createTokenRequest(String url) {
        return createTokenRequest(new URL(url));
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final String redirectUri = getConfiguration().getRedirectUrl();
        final String baseAuthUrl = getConfiguration().getBaseAuthUrl();

        final InitialTokenResponse clientCredentials = getInitialTokenResponse();
        final AccessConsentResponse accessConsentResponse = getAccessConsent(clientCredentials);

        sessionStorage.put(StorageKeys.CONSENT, accessConsentResponse);

        final JwtHeader jwtHeader = new JwtHeader(OIDCValues.ALG, OIDCValues.TYP);
        final JwtAuthPayload jwtAuthPayload =
                new JwtAuthPayload(
                        OIDCValues.SCOPE,
                        new IdTokenClaim(
                                OIDCValues.TOKEN_ID_PREFIX
                                        + accessConsentResponse.getData().getConsentId(),
                                false),
                        clientId,
                        redirectUri,
                        state,
                        state,
                        clientId);

        final String oidcRequest =
                JwtUtils.toOidcBase64(
                        eidasProxyConfiguration, "Tink", null, jwtHeader, jwtAuthPayload);

        return client.request(baseAuthUrl + Urls.OAUTH)
                .header(HeaderKeys.X_API_KEY, clientSecret)
                .queryParam(QueryKeys.REQUEST, oidcRequest)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.NONCE, state)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE_OPENID)
                .getUrl();
    }

    private InitialTokenResponse getInitialTokenResponse() {
        final String clientSecret = getConfiguration().getClientSecret();

        final String baseApiUrl = getConfiguration().getBaseAPIUrl();

        return createTokenRequest(baseApiUrl + Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.X_API_KEY, clientSecret)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.CLIENT_CREDENTIALS)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .post(InitialTokenResponse.class);
    }

    private AccessConsentResponse getAccessConsent(InitialTokenResponse clientCredentials) {
        final String baseApiUrl = getConfiguration().getBaseAPIUrl();
        final String url = baseApiUrl + Urls.ACCOUNT_ACCESS_CONSENTS;

        final AccessConsentRequest accessConsentRequest =
                new AccessConsentRequest(
                        new RequestDataEntity(
                                "", Arrays.asList(OIDCValues.CONSENT_PERMISSIONS), "", ""),
                        new RiskEntity());

        return createAuthorizationRequest(clientCredentials, url)
                .post(AccessConsentResponse.class, accessConsentRequest);
    }

    public OAuth2Token getToken(String code) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String baseApiUrl = getConfiguration().getBaseAPIUrl();

        return createTokenRequest(baseApiUrl + Urls.TOKEN)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE)
                .queryParam(QueryKeys.CODE, code)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public CrosskeyAccountsResponse fetchAccounts() {
        final String baseApiUrl = getConfiguration().getBaseAPIUrl();

        return createRequestInSession(baseApiUrl + Urls.ACCOUNTS)
                .get(CrosskeyAccountsResponse.class);
    }

    public CrosskeyAccountBalancesResponse fetchAccountBalances(String accountId) {
        final String baseApiUrl = getConfiguration().getBaseAPIUrl();
        final URL url =
                new URL(baseApiUrl + Urls.ACCOUNT_BALANCES)
                        .parameter(UrlParameters.ACCOUNT_ID, accountId);

        return createRequestInSession(url).get(CrosskeyAccountBalancesResponse.class);
    }

    public CrosskeyTransactionsResponse fetchCreditCardTransactions(CreditCardAccount account) {

        final String baseApiUrl = getConfiguration().getBaseAPIUrl();
        final URL url =
                new URL(baseApiUrl + Urls.ACCOUNT_TRANSACTIONS)
                        .parameter(UrlParameters.ACCOUNT_ID, account.getApiIdentifier());

        return createRequestInSession(url)
                .get(CrosskeyTransactionsResponse.class)
                .setTransactionType(TransactionTypeEntity.CREDIT);
    }

    public CrosskeyTransactionsResponse fetchTransactionalAccountTransactions(
            TransactionalAccount account) {

        final String baseApiUrl = getConfiguration().getBaseAPIUrl();
        final URL url =
                new URL(baseApiUrl + Urls.ACCOUNT_TRANSACTIONS)
                        .parameter(UrlParameters.ACCOUNT_ID, account.getApiIdentifier());

        return createRequestInSession(url)
                .get(CrosskeyTransactionsResponse.class)
                .setTransactionType(TransactionTypeEntity.DEBIT);
    }

    public OAuth2Token getRefreshToken(String refreshToken) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String baseApiUrl = getConfiguration().getBaseAPIUrl();

        return createTokenRequest(baseApiUrl + Urls.TOKEN)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.REFRESH_TOKEN)
                .queryParam(QueryKeys.REFRESH_TOKEN, refreshToken)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.TOKEN, accessToken);
    }
}
