package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.AuthorizationCodeTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.RefreshTokenTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.util.CodeChallengeUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.util.SignatureUtil;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class CmcicApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final CmcicConfiguration configuration;
    private final EidasProxyConfiguration eidasProxyConfiguration;
    private final EidasIdentity eidasIdentity;

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createPispRequestInSession(
            URL baseUrl, String path, HttpMethod httpMethod) {
        return createRequestInSession(baseUrl, path, httpMethod, getPispTokenFromStorage());
    }

    private RequestBuilder createPispRequestInSession(
            URL baseUrl, String path, String body, HttpMethod httpMethod) {
        return createRequestInSession(baseUrl, path, body, httpMethod, getPispTokenFromStorage());
    }

    private RequestBuilder createAispRequestInSession(
            URL baseUrl, String path, HttpMethod httpMethod) {
        return createRequestInSession(baseUrl, path, httpMethod, getAispTokenFromStorage());
    }

    private RequestBuilder createRequestInSession(
            URL baseUrl, String path, String body, HttpMethod httpMethod, OAuth2Token authToken) {

        final String date = getServerTime();
        final String digest = SignatureUtil.generateDigest(body);
        final String requestId = UUID.randomUUID().toString();
        URL requestUrl = baseUrl.concat(path);

        String signatureHeaderValue =
                SignatureUtil.getSignatureHeaderValue(
                        configuration.getKeyId(),
                        httpMethod.name(),
                        requestUrl.toUri(),
                        date,
                        digest,
                        MediaType.APPLICATION_JSON,
                        requestId,
                        eidasProxyConfiguration,
                        eidasIdentity);

        return client.request(requestUrl)
                .addBearerToken(authToken)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.HOST, requestUrl.toUri().getHost())
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.SIGNATURE, signatureHeaderValue)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(
            URL baseUrl, String path, HttpMethod httpMethod, OAuth2Token authToken) {

        final String date = getServerTime();
        final String requestId = UUID.randomUUID().toString();
        URL requestUrl = baseUrl.concat(path);

        String signatureHeaderValue =
                SignatureUtil.getSignatureHeaderValue(
                        configuration.getKeyId(),
                        httpMethod.name(),
                        requestUrl.toUri(),
                        date,
                        requestId,
                        eidasProxyConfiguration,
                        eidasIdentity);

        return client.request(requestUrl)
                .addBearerToken(authToken)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.HOST, requestUrl.toUri().getHost())
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.SIGNATURE, signatureHeaderValue)
                .header(HeaderKeys.X_REQUEST_ID, requestId);
    }

    private OAuth2Token getPispTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.PISP_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    private OAuth2Token getAispTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL getAuthorizeUrl(String state) {
        return client.request(configuration.getAuthBaseUrl())
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE_CHALLENGE, getCodeChallenge())
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    private String getCodeChallenge() {
        final String codeVerifier = CodeChallengeUtil.generateCodeVerifier();
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        return CodeChallengeUtil.generateCodeChallengeForCodeVerifier(codeVerifier);
    }

    public FetchAccountsResponse fetchAccounts() {
        String baseUrl = configuration.getBaseUrl();
        String basePath = configuration.getBasePath();

        return createAispRequestInSession(
                        new URL(baseUrl), basePath + Urls.FETCH_ACCOUNTS_PATH, HttpMethod.GET)
                .get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, URL nextUrl) {
        URL baseUrl = new URL(configuration.getBaseUrl());
        String basePath = configuration.getBasePath();

        String path =
                Optional.ofNullable(nextUrl)
                        .map(URL::get)
                        .orElseGet(
                                () ->
                                        basePath
                                                + String.format(
                                                        Urls.FETCH_TRANSACTIONS_PATH,
                                                        account.getApiIdentifier()));

        FetchTransactionsResponse fetchTransactionsResponse =
                createAispRequestInSession(baseUrl, path, HttpMethod.GET)
                        .get(FetchTransactionsResponse.class);

        fetchTransactionsResponse.setTransactionalAccount(account);

        return fetchTransactionsResponse;
    }

    public HalPaymentRequestCreation makePayment(
            PaymentRequestResourceEntity paymentRequestResourceEntity) {

        String baseUrl = configuration.getBaseUrl();
        String basePath = configuration.getBasePath();

        String body = SerializationUtils.serializeToString(paymentRequestResourceEntity);

        return createPispRequestInSession(
                        new URL(baseUrl), basePath + Urls.PAYMENT_REQUESTS, body, HttpMethod.POST)
                .type(MediaType.APPLICATION_JSON)
                .post(HalPaymentRequestCreation.class, body);
    }

    public OAuth2Token getAispToken(String code) {
        final AuthorizationCodeTokenRequest request =
                new AuthorizationCodeTokenRequest(
                        configuration.getClientId(),
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));
        return executeTokenRequest(request);
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        final RefreshTokenTokenRequest request =
                new RefreshTokenTokenRequest(
                        configuration.getClientId(), refreshToken, FormValues.REFRESH_TOKEN);
        return executeTokenRequestAndCheckTokenNotExpired(request);
    }

    private OAuth2Token executeTokenRequest(AbstractForm request) {
        final URL tokenUrl = getTokenUrl();
        final TokenResponse tokenResponse = getTokenResponse(request, tokenUrl);

        return createOAuth2Token(tokenResponse);
    }

    private OAuth2Token executeTokenRequestAndCheckTokenNotExpired(AbstractForm request)
            throws SessionException {
        final URL tokenUrl = getTokenUrl();
        final TokenResponse tokenResponse = getTokenResponseAndCheckTokenExpired(request, tokenUrl);

        return createOAuth2Token(tokenResponse);
    }

    public HalPaymentRequestEntity fetchPayment(String uniqueId) {
        String baseUrl = configuration.getBaseUrl();
        String basePath = configuration.getBasePath();

        return createPispRequestInSession(
                        new URL(baseUrl),
                        basePath + Urls.PAYMENT_REQUESTS + "/" + uniqueId,
                        HttpMethod.GET)
                .type(MediaType.APPLICATION_JSON)
                .get(HalPaymentRequestEntity.class);
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Signature.DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Signature.TIMEZONE));
        return dateFormat.format(calendar.getTime());
    }

    public EndUserIdentityResponse getEndUserIdentity() {
        String baseUrl = configuration.getBaseUrl();
        String basePath = configuration.getBasePath();

        return createAispRequestInSession(
                        new URL(baseUrl), basePath + Urls.FETCH_END_USER_IDENTITY, HttpMethod.GET)
                .get(EndUserIdentityResponse.class);
    }

    private TokenResponse getTokenResponse(AbstractForm request, URL tokenUrl) {
        return createRequest(tokenUrl)
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class);
    }

    private TokenResponse getTokenResponseAndCheckTokenExpired(AbstractForm request, URL tokenUrl)
            throws SessionException {
        try {
            return createRequest(tokenUrl)
                    .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                    .post(TokenResponse.class);
        } catch (HttpResponseException ex) {
            if (hasRefreshTokenExpired(ex)) {
                throw SessionError.SESSION_EXPIRED.exception();
            } else {
                throw ex;
            }
        }
    }

    private URL getTokenUrl() {
        final String baseUrl = configuration.getBaseUrl();
        final String basePath = configuration.getBasePath();
        final URL baseApiUrl = new URL(baseUrl + basePath);

        return baseApiUrl.concat(Urls.TOKEN_PATH);
    }

    private static OAuth2Token createOAuth2Token(TokenResponse tokenResponse) {
        return OAuth2Token.create(
                tokenResponse.getTokenType().toString(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn());
    }

    private static boolean hasRefreshTokenExpired(HttpResponseException ex) {
        return ex.getMessage().toLowerCase().contains("refresh token has expired");
    }
}
