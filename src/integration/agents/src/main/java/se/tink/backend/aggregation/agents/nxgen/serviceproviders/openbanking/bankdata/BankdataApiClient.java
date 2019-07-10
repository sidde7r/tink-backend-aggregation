package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.InitialTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.configuration.BankdataConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.utils.BankdataUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.utils.DateUtils;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class BankdataApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private BankdataConfiguration configuration;

    public BankdataApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private BankdataConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(BankdataConfiguration configuration) {
        this.configuration = configuration;
        this.client.setEidasProxy(this.configuration.getEidasProxyBaseUrl(), "Tink");
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromSession();

        return createRequest(url).addBearerToken(authToken);
    }

    private AccountEntity fetchBalances(final AccountEntity accountEntity) {
        final String requestId = UUID.randomUUID().toString();
        final URL url =
                new URL(
                        getConfiguration().getBaseUrl()
                                + Endpoints.AIS_PRODUCT
                                + accountEntity.getBalancesLink());

        final List<BalanceEntity> balances =
                client.request(url)
                        .addBearerToken(getTokenFromSession())
                        .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                        .header(HeaderKeys.X_REQUEST_ID, requestId)
                        .type(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .get(AccountEntity.class)
                        .getBalances();
        accountEntity.setBalances(balances);

        return accountEntity;
    }

    public AccountResponse fetchAccounts() {
        final String requestId = UUID.randomUUID().toString();
        URL url = new URL(configuration.getBaseUrl() + Endpoints.ACCOUNTS);

        final AccountResponse accountsWithoutBalances =
                createRequestInSession(url)
                        .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                        .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                        .header(HeaderKeys.X_REQUEST_ID, requestId)
                        .type(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .get(AccountResponse.class);

        final List<AccountEntity> accountsWithBalances =
                accountsWithoutBalances.getAccounts().stream()
                        .map(this::fetchBalances)
                        .collect(Collectors.toList());

        return new AccountResponse(accountsWithBalances);
    }

    public TransactionResponse fetchTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final String requestId = UUID.randomUUID().toString();
        final URL fullUrl =
                new URL(
                        configuration.getBaseUrl()
                                + Endpoints.AIS_PRODUCT
                                + account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL));

        return createRequestInSession(fullUrl)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.CONSENT_ID, sessionStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(
                        QueryKeys.DATE_TO,
                        DateUtils.formatDateTime(toDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.DATE_FROM,
                        DateUtils.formatDateTime(fromDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .get(TransactionResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        String consentId = getConsentId();
        authorizeConsent(consentId);
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String codeVerifier = BankdataUtils.generateCodeVerifier();
        final String codeChallenge = BankdataUtils.generateCodeChallenge(codeVerifier);
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        sessionStorage.put(StorageKeys.CONSENT_ID, consentId);
        URL url = new URL(configuration.getBaseAuthUrl() + Endpoints.AUTHORIZE);

        return createRequest(url)
                .queryParam(QueryKeys.RESPONSE_TYPE, BankdataConstants.QueryValues.CODE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.SCOPE, "ais:" + consentId)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam("acr", "psd2")
                .getUrl();
    }

    private void getTokenWithClientCredentials() {
        final InitialTokenRequest request =
                new InitialTokenRequest(
                        FormValues.CLIENT_CREDENTIALS,
                        FormValues.SCOPE,
                        getConfiguration().getClientId());
        URL url = new URL(configuration.getBaseAuthUrl() + Endpoints.TOKEN);

        TokenResponse response =
                client.request(url)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(TokenResponse.class, request.toData());

        sessionStorage.put(StorageKeys.OAUTH_TOKEN, response.toTinkToken());
    }

    public String getConsentId() {
        getTokenWithClientCredentials();
        final ConsentRequest consentRequest = new ConsentRequest();
        final String requestId = UUID.randomUUID().toString();
        URL url = new URL(configuration.getBaseUrl() + Endpoints.CONSENT);

        ConsentResponse response =
                client.request(url)
                        .addBearerToken(getTokenFromSession())
                        .header(HeaderKeys.X_API_KEY, getConfiguration().getApiKey())
                        .header(HeaderKeys.X_REQUEST_ID, requestId)
                        .body(consentRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                        .post(ConsentResponse.class);

        return response.getConsentId();
    }

    private void authorizeConsent(String consentId) {
        final String requestId = UUID.randomUUID().toString();
        final ConsentAuthorizationRequest consentAuthorization = new ConsentAuthorizationRequest();
        URL url = new URL(configuration.getBaseUrl() + Endpoints.AUTHORIZE_CONSENT);

        HttpResponse response =
                client.request(url.parameter(IdTags.CONSENT_ID, consentId))
                        .addBearerToken(getTokenFromSession())
                        .header(HeaderKeys.X_API_KEY, getConfiguration().getApiKey())
                        .header(HeaderKeys.X_REQUEST_ID, requestId)
                        .body(consentAuthorization, MediaType.APPLICATION_JSON_TYPE)
                        .post(HttpResponse.class);
    }

    public OAuth2Token getToken(String code) {
        final AccessTokenRequest request =
                new AccessTokenRequest(
                        BankdataConstants.FormValues.AUTHORIZATION_CODE,
                        getConfiguration().getClientId(),
                        code,
                        getConfiguration().getRedirectUrl(),
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        return getTokenResponse(request);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public void setTokenToSession(OAuth2Token accessToken, String storageKey) {
        sessionStorage.put(storageKey, accessToken);
    }

    public OAuth2Token refreshToken(String refreshToken) {
        final RefreshTokenRequest request =
                new RefreshTokenRequest(
                        FormValues.REFRESH_TOKEN_GRANT_TYPE,
                        refreshToken,
                        getConfiguration().getRedirectUrl(),
                        getConfiguration().getClientId(),
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        return getTokenResponse(request);
    }

    private OAuth2Token getTokenResponse(TokenRequest tokenRequest) {
        URL url = new URL(configuration.getBaseAuthUrl() + Endpoints.TOKEN);

        return client.request(url)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class, tokenRequest.toData())
                .toTinkToken();
    }
}
