package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JwtPaymentConsentHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.InitialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.rpc.CrosskeyPaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction.TransactionTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.JwtHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.JwtUtils;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CrosskeyBaseApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private CrosskeyBaseConfiguration configuration;
    private String redirectUrl;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;
    private final String baseAuthUrl;
    private final String baseApiUrl;
    private final String xFapiFinancialId;
    private String certificateSerialNumber;

    public CrosskeyBaseApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            CrosskeyMarketConfiguration marketConfiguration) {
        this.client = client;
        client.setTimeout(60 * 1000);
        this.sessionStorage = sessionStorage;
        this.baseAuthUrl = marketConfiguration.getBaseAuthURL();
        this.baseApiUrl = marketConfiguration.getBaseApiURL();
        this.xFapiFinancialId = marketConfiguration.getFinancialId();
    }

    public CrosskeyBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(
            AgentConfiguration<CrosskeyBaseConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
        this.client.setEidasProxy(eidasProxyConfiguration);
        try {
            this.certificateSerialNumber =
                    CertificateUtils.getSerialNumber(agentConfiguration.getQsealc(), 10);
        } catch (CertificateException e) {
            throw new IllegalStateException(ErrorMessages.INVALID_CONFIGURATION, e);
        }
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String clientSecret = getConfiguration().getClientSecret();

        return createRequest(url)
                .addBearerToken(getTokenFromSession())
                .header(HeaderKeys.X_API_KEY, clientSecret)
                .header(HeaderKeys.X_FAPI_FINANCIAL_ID, xFapiFinancialId);
    }

    private RequestBuilder createAuthorizationRequest(
            InitialTokenResponse clientCredentials, URL url) {
        final String clientSecret = getConfiguration().getClientSecret();

        return createRequest(url)
                .addBearerToken(clientCredentials.toTinkToken())
                .header(HeaderKeys.X_API_KEY, clientSecret)
                .header(HeaderKeys.X_FAPI_FINANCIAL_ID, xFapiFinancialId);
    }

    private RequestBuilder createTokenRequest(URL url) {
        final String clientId = getConfiguration().getClientId();

        return client.request(url)
                .header(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(QueryKeys.CLIENT_ID, clientId);
    }

    public InitialTokenResponse getClientCredentialsToken() {
        final String clientSecret = getConfiguration().getClientSecret();
        final URL url = new URL(baseApiUrl + CrosskeyBaseConstants.Urls.TOKEN);

        return createTokenRequest(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.X_API_KEY, clientSecret)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.CLIENT_CREDENTIALS)
                .queryParam(QueryKeys.SCOPE, OIDCValues.SCOPE_ALL)
                .post(InitialTokenResponse.class);
    }

    private AccessConsentResponse getAccessConsent(InitialTokenResponse clientCredentials) {
        final String url = baseApiUrl + CrosskeyBaseConstants.Urls.ACCOUNT_ACCESS_CONSENTS;

        final AccessConsentRequest accessConsentRequest =
                new AccessConsentRequest(
                        new RequestDataEntity(
                                "", Arrays.asList(OIDCValues.CONSENT_PERMISSIONS), "", ""),
                        new RiskEntity());

        return createAuthorizationRequest(clientCredentials, new URL(url))
                .post(AccessConsentResponse.class, accessConsentRequest);
    }

    public OAuth2Token getToken(String code) {
        final String redirectUri = getRedirectUrl();
        final URL url = new URL(baseApiUrl + CrosskeyBaseConstants.Urls.TOKEN);

        return createTokenRequest(url)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.AUTHORIZATION_CODE)
                .queryParam(QueryKeys.CODE, code)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public CrosskeyAccountsResponse fetchAccounts() {
        final URL url = new URL(baseApiUrl + CrosskeyBaseConstants.Urls.ACCOUNTS);

        return createRequestInSession(url).get(CrosskeyAccountsResponse.class);
    }

    public CrosskeyAccountBalancesResponse fetchAccountBalances(String accountId) {
        final URL url =
                new URL(baseApiUrl + CrosskeyBaseConstants.Urls.ACCOUNT_BALANCES)
                        .parameter(UrlParameters.ACCOUNT_ID, accountId);

        return createRequestInSession(url).get(CrosskeyAccountBalancesResponse.class);
    }

    public CrosskeyTransactionsResponse fetchCreditCardTransactions(CreditCardAccount account) {
        final URL url =
                new URL(baseApiUrl + CrosskeyBaseConstants.Urls.ACCOUNT_TRANSACTIONS)
                        .parameter(UrlParameters.ACCOUNT_ID, account.getApiIdentifier());

        return createRequestInSession(url)
                .get(CrosskeyTransactionsResponse.class)
                .setTransactionType(TransactionTypeEntity.CREDIT);
    }

    public CrosskeyTransactionsResponse fetchTransactionalAccountTransactions(
            TransactionalAccount account) {
        final URL url =
                new URL(baseApiUrl + CrosskeyBaseConstants.Urls.ACCOUNT_TRANSACTIONS)
                        .parameter(UrlParameters.ACCOUNT_ID, account.getApiIdentifier());

        return createRequestInSession(url)
                .get(CrosskeyTransactionsResponse.class)
                .setTransactionType(TransactionTypeEntity.DEBIT);
    }

    public OAuth2Token getRefreshToken(String refreshToken) {
        final String redirectUri = getRedirectUrl();
        final URL url = new URL(baseApiUrl + CrosskeyBaseConstants.Urls.TOKEN);

        return createTokenRequest(url)
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

    private JwtPaymentConsentHeader getJwtHeader() {
        return new JwtPaymentConsentHeader(
                OIDCValues.B_64,
                certificateSerialNumber,
                OIDCValues.ALG,
                Arrays.asList(OIDCValues.B_64_STR, OIDCValues.IAT, OIDCValues.ISS, OIDCValues.TAN),
                // -3 since it is sometimes rounds up to future..
                Instant.now().getEpochSecond() - 3,
                baseApiUrl,
                getConfiguration().getClientId());
    }

    private <Header, Payload> String createJwsHeader(Header header, Payload payload) {
        final String serializedToJsonPayload = SerializationUtils.serializeToString(payload);

        final String serializedToJsonHeader = SerializationUtils.serializeToString(header);

        final String headerBase64 =
                Base64.getUrlEncoder()
                        .encodeToString(serializedToJsonHeader.getBytes(StandardCharsets.UTF_8));

        final String headerBase64WithPayload =
                String.format("%s.%s", headerBase64, serializedToJsonPayload);

        final String signedBase64HeadersAndPayload =
                QsealcSignerImpl.build(
                                eidasProxyConfiguration.toInternalConfig(),
                                QsealcAlg.EIDAS_RSA_SHA256,
                                eidasIdentity)
                        .getSignatureBase64(headerBase64WithPayload.getBytes());

        return String.format("%s..%s", headerBase64, signedBase64HeadersAndPayload);
    }

    private RequestBuilder getPaymentConsent(String jwsHeaderValue) {
        final URL url = new URL(baseApiUrl + CrosskeyBaseConstants.Urls.PAYMENT_ACCESS_CONSENTS);

        return createPaymentConsent(url)
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_IDEMPOTENCY_KEY,
                        Psd2Headers.getRequestId())
                .header(HeaderKeys.X_JWS_SIGNATURE, jwsHeaderValue);
    }

    public CrosskeyPaymentDetails createPaymentConsent(
            CrosskeyPaymentDetails crosskeyPaymentDetails) {
        JwtPaymentConsentHeader jwtHeader = getJwtHeader();
        String headerAndSignatureJwt = createJwsHeader(jwtHeader, crosskeyPaymentDetails);
        RequestBuilder paymentRequest = getPaymentConsent(headerAndSignatureJwt);
        return paymentRequest.body(crosskeyPaymentDetails).post(CrosskeyPaymentDetails.class);
    }

    public CrosskeyPaymentDetails makePayment(CrosskeyPaymentDetails paymentDetail) {
        JwtPaymentConsentHeader jwtHeader = getJwtHeader();
        String jws = createJwsHeader(jwtHeader, paymentDetail);
        final URL url = new URL(baseApiUrl + CrosskeyBaseConstants.Urls.MAKE_PAYMENT);

        return createPaymentConsent(url)
                .body(paymentDetail)
                .header(HeaderKeys.X_JWS_SIGNATURE, jws)
                .header(HeaderKeys.X_IDEMPOTENCY_KEY, Psd2Headers.getRequestId())
                .body(paymentDetail)
                .post(CrosskeyPaymentDetails.class);
    }

    private RequestBuilder createPaymentConsent(URL url) {
        return createRequestInSession(url)
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_FAPI_INTERACTION_ID,
                        Psd2Headers.getRequestId());
    }

    public URL getPisAuthorizeUrl(String state) {

        String oidcRequest =
                getOIDCRequest(
                        state,
                        OIDCValues.SCOPE_PAYMENTS,
                        OIDCValues.TOKEN_ID_PREFIX_PAYMENT,
                        getConsentIdFromStorage());

        return getAuthorizeUrl(state, oidcRequest, OIDCValues.SCOPE_PAYMENTS);
    }

    public URL getAisAuthorizeUrl(String state) {
        final InitialTokenResponse clientCredentials = getClientCredentialsToken();
        final AccessConsentResponse accessConsentResponse = getAccessConsent(clientCredentials);

        sessionStorage.put(StorageKeys.CONSENT, accessConsentResponse);

        String oidcRequest =
                getOIDCRequest(
                        state,
                        OIDCValues.SCOPE_ACCOUNTS,
                        OIDCValues.TOKEN_ID_PREFIX_ACCOUNT,
                        accessConsentResponse.getData().getConsentId());

        return getAuthorizeUrl(state, oidcRequest, OIDCValues.SCOPE_ACCOUNTS);
    }

    private URL getAuthorizeUrl(String state, String oidcRequest, String scope) {

        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        final String redirectUri = getRedirectUrl();

        return client.request(baseAuthUrl + CrosskeyBaseConstants.Urls.OAUTH)
                .header(HeaderKeys.X_API_KEY, clientSecret)
                .queryParam(QueryKeys.REQUEST, oidcRequest)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.NONCE, state)
                .queryParam(QueryKeys.SCOPE, scope)
                .getUrl();
    }

    private String getOIDCRequest(
            String state, String scope, String tokenIdPrefix, String consentId) {

        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getRedirectUrl();
        final JwtHeader jwtHeader = new JwtHeader(OIDCValues.ALG, OIDCValues.TYP);
        final JwtAuthPayload jwtAuthPayload =
                new JwtAuthPayload(
                        scope,
                        new IdTokenClaim(tokenIdPrefix + consentId, false),
                        clientId,
                        redirectUri,
                        state,
                        state,
                        clientId);

        return JwtUtils.toOidcBase64(
                eidasProxyConfiguration.toInternalConfig(),
                jwtHeader,
                eidasIdentity,
                jwtAuthPayload);
    }

    private String getConsentIdFromStorage() {
        Optional<CrosskeyPaymentDetails> consentDetails =
                sessionStorage.get(StorageKeys.CONSENT, CrosskeyPaymentDetails.class);
        return consentDetails
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.CONSENT_ID_NOT_FOUND))
                .getData()
                .getConsentId();
    }

    public CrosskeyPaymentDetails fetchPayment(String paymentId) {
        final URL url =
                new URL(baseApiUrl + Urls.FETCH_PAYMENT)
                        .parameter(UrlParameters.INTERNATIONAL_PAYMENT_ID, paymentId);

        return createRequestInSession(url)
                .header(
                        CrosskeyBaseConstants.HeaderKeys.X_IDEMPOTENCY_KEY,
                        Psd2Headers.getRequestId())
                .get(CrosskeyPaymentDetails.class);
    }
}
