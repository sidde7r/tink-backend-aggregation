package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.Encryption;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.Format;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JWTAuthPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.entities.oidcrequestobject.JWTHeader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.InitialTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction.TransactionTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils.JWTUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import tink.org.apache.http.HttpHeaders;

public class CrosskeyBaseApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private CrosskeyBaseConfiguration configuration;

    public CrosskeyBaseApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void setConfiguration(CrosskeyBaseConfiguration configuration) {
        this.configuration = configuration;
    }

    public CrosskeyBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
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
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .queryParam(QueryKeys.CLIENT_ID, clientId);
    }

    private RequestBuilder createTokenRequest(String url) {
        return createTokenRequest(new URL(url));
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final String redirectUri = getConfiguration().getRedirectUrl();
        final String keyPath = getConfiguration().getClientSigningKeyPath();
        final String baseAuthUrl = getConfiguration().getBaseAuthUrl();

        final InitialTokenResponse clientCredentials = getInitialTokenResponse();
        final AccessConsentResponse accessConsentResponse = getAccessConsent(clientCredentials);

        sessionStorage.put(StorageKeys.CONSENT, accessConsentResponse);

        final JWTHeader jwtHeader = new JWTHeader(OIDCValues.ALG, OIDCValues.TYP);
        final JWTAuthPayload jwtAuthPayload =
                new JWTAuthPayload(
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
                JWTUtils.constructOIDCRequestObject(
                        jwtHeader, jwtAuthPayload, keyPath, Encryption.KEY_ALGORITHM);

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

    public PaginatorResponse fetchCreditCardTransactions(
            CreditCardAccount account, Date fromDate, Date toDate) {
        final AccessConsentResponse accessConsentFromSession = getAccessConsentFromSession();

        final Date restrictedFromDate =
                DateUtils.getRestrictedDate(
                        fromDate,
                        accessConsentFromSession.getData().getTransactionFromDateTime(),
                        Date::after);
        final Date restrictedToDate =
                DateUtils.getRestrictedDate(
                        toDate,
                        accessConsentFromSession.getData().getTransactionToDateTime(),
                        Date::before);

        if (toDate.before(fromDate)) {
            return new CrosskeyTransactionsResponse();
        }

        // TODO Use apiIdentifier once framework is updated
        return getTransactionsResponse(account.getBankIdentifier(), fromDate, toDate)
                .setTransactionType(TransactionTypeEntity.CREDIT);
    }

    public PaginatorResponse fetchTransactionalAccountTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final AccessConsentResponse accessConsentFromSession = getAccessConsentFromSession();

        final Date restrictedFromDate =
                DateUtils.getRestrictedDate(
                        fromDate,
                        accessConsentFromSession.getData().getTransactionFromDateTime(),
                        Date::after);

        final Date restrictedToDate =
                DateUtils.getRestrictedDate(
                        toDate,
                        accessConsentFromSession.getData().getTransactionToDateTime(),
                        Date::before);

        if (toDate.before(fromDate)) {
            return new CrosskeyTransactionsResponse();
        }

        return getTransactionsResponse(account.getApiIdentifier(), fromDate, toDate)
                .setTransactionType(TransactionTypeEntity.DEBIT);
    }

    private CrosskeyTransactionsResponse getTransactionsResponse(
            String apiIdentifier, Date fromDate, Date toDate) {
        final String baseApiUrl = getConfiguration().getBaseAPIUrl();
        final URL url =
                new URL(baseApiUrl + Urls.ACCOUNT_TRANSACTIONS)
                        .parameter(UrlParameters.ACCOUNT_ID, apiIdentifier);

        return createRequestInSession(url)
                .queryParam(
                        QueryKeys.FROM_BOOKING_DATE_TIME,
                        DateUtils.formatDateTime(fromDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.TO_BOOKING_DATE_TIME,
                        DateUtils.formatDateTime(toDate, Format.TIMESTAMP, Format.TIMEZONE))
                .get(CrosskeyTransactionsResponse.class);
    }

    private AccessConsentResponse getConsent(
            InitialTokenResponse clientCredentials, String consentId) {
        final String baseApiUrl = getConfiguration().getBaseAPIUrl();
        final URL url =
                new URL(baseApiUrl + Urls.ACCOUNT_ACCESS_CONSENT)
                        .parameter(UrlParameters.CONSENT_ID, consentId);

        return createAuthorizationRequest(clientCredentials, url).get(AccessConsentResponse.class);
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
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        CrosskeyBaseConstants.Exceptions.MISSING_TOKEN));
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.TOKEN, accessToken);
    }

    private AccessConsentResponse getAccessConsentFromSession() {
        return sessionStorage
                .get(StorageKeys.CONSENT, AccessConsentResponse.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        CrosskeyBaseConstants.Exceptions.MISSING_CONSENT));
    }
}
