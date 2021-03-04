package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls.BASE_API_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls.BENEFICIARIES_PATH;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.AuthorizationCodeTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.PisTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.RefreshTokenTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.ConfirmationResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicCodeChallengeProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicDigestProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider.CmcicSignatureProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.FrAispApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.dto.TrustedBeneficiariesResponseDto;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class CmcicApiClient implements FrAispApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final CmcicConfiguration configuration;
    private final CmcicDigestProvider digestProvider;
    private final CmcicSignatureProvider signatureProvider;
    private final CmcicCodeChallengeProvider codeChallengeProvider;
    private final CmcicAgentConfig cmcicAgentConfig;

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createPispRequestInSession(String baseUrl, String path) {
        return createRequestInSession(
                baseUrl,
                path,
                getPispTokenFromStorage()
                        .filter(OAuth2Token::isValid)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.NO_PIS_OAUTH_TOKEN_IN_STORAGE)));
    }

    private RequestBuilder createPispRequestInSession(URL baseUrl, String path, String body) {
        return createRequestInSession(
                baseUrl,
                path,
                body,
                getPispTokenFromStorage()
                        .filter(OAuth2Token::isValid)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.NO_PIS_OAUTH_TOKEN_IN_STORAGE)));
    }

    private RequestBuilder createAispRequestInSession(String baseUrl, String path) {
        return createRequestInSession(baseUrl, path, getAispTokenFromStorage());
    }

    private RequestBuilder createRequestInSession(
            URL baseUrl, String path, String body, OAuth2Token authToken) {

        final String date = getServerTime();
        final String digest = digestProvider.generateDigest(body);
        final String requestId = UUID.randomUUID().toString();
        URL requestUrl = baseUrl.concat(path);

        String signatureHeaderValue =
                signatureProvider.getSignatureHeaderValueForPost(
                        configuration.getKeyId(), requestUrl.toUri(), date, digest, requestId);

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
            String baseUrl, String path, OAuth2Token authToken) {

        final String date = getServerTime();
        final String requestId = UUID.randomUUID().toString();
        URL requestUrl = new URL(baseUrl + path);

        String signatureHeaderValue =
                signatureProvider.getSignatureHeaderValueForGet(
                        configuration.getKeyId(), requestUrl.toUri(), date, requestId);

        return client.request(requestUrl)
                .addBearerToken(authToken)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.HOST, requestUrl.toUri().getHost())
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.SIGNATURE, signatureHeaderValue)
                .header(HeaderKeys.X_REQUEST_ID, requestId);
    }

    private Optional<OAuth2Token> getPispTokenFromStorage() {
        return sessionStorage.get(StorageKeys.PISP_TOKEN, OAuth2Token.class);
    }

    private OAuth2Token getAispTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL getAuthorizeUrl(String state) {
        return client.request(cmcicAgentConfig.getAuthBaseUrl())
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE_CHALLENGE, getCodeChallenge())
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    private String getCodeChallenge() {
        final String codeVerifier = codeChallengeProvider.generateCodeVerifier();
        sessionStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        return codeChallengeProvider.generateCodeChallengeForCodeVerifier(codeVerifier);
    }

    public FetchAccountsResponse fetchAccounts() {
        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();

        return createAispRequestInSession(baseUrl, basePath + Urls.FETCH_ACCOUNTS_PATH)
                .get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(Account account, URL nextUrl) {
        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();

        String path =
                Optional.ofNullable(nextUrl)
                        .map(URL::get)
                        .orElseGet(
                                () ->
                                        basePath
                                                + String.format(
                                                        Urls.FETCH_TRANSACTIONS_PATH,
                                                        account.getApiIdentifier()));

        return createAispRequestInSession(baseUrl, path).get(FetchTransactionsResponse.class);
    }

    public HalPaymentRequestCreation makePayment(
            PaymentRequestResourceEntity paymentRequestResourceEntity) {

        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();

        String body = SerializationUtils.serializeToString(paymentRequestResourceEntity);

        return createPispRequestInSession(new URL(baseUrl), basePath + Urls.PAYMENT_REQUESTS, body)
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

    public void fetchPisOauthToken() {
        if (!isValidPisOauthToken()) {
            fetchAndSavePisOauthToken();
        }
    }

    private boolean isValidPisOauthToken() {
        return getPispTokenFromStorage().map(OAuth2Token::isValid).orElse(false);
    }

    private void fetchAndSavePisOauthToken() {
        PisTokenRequest pisTokenRequest = new PisTokenRequest(configuration.getClientId());
        OAuth2Token token = executeTokenRequest(pisTokenRequest);
        sessionStorage.put(StorageKeys.PISP_TOKEN, token);
    }

    public HalPaymentRequestEntity fetchPayment(String uniqueId) {
        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();

        return createPispRequestInSession(
                        baseUrl, basePath + Urls.PAYMENT_REQUESTS + "/" + uniqueId)
                .type(MediaType.APPLICATION_JSON)
                .get(HalPaymentRequestEntity.class);
    }

    public HalPaymentRequestEntity confirmPayment(
            String uniqueId, ConfirmationResourceEntity confirmationResourceEntity) {
        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();
        String body = SerializationUtils.serializeToString(confirmationResourceEntity);
        return createPispRequestInSession(
                        new URL(baseUrl),
                        String.format(
                                "%s%s%s%s%s%s",
                                basePath,
                                Urls.PAYMENT_REQUESTS,
                                "/",
                                uniqueId,
                                "/",
                                Urls.PIS_CONFIRMATION_PATH),
                        body)
                .type(MediaType.APPLICATION_JSON)
                .post(HalPaymentRequestEntity.class, body);
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(Signature.DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone(Signature.TIMEZONE));
        return dateFormat.format(calendar.getTime());
    }

    public EndUserIdentityResponse getEndUserIdentity() {
        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();

        return createAispRequestInSession(baseUrl, basePath + Urls.FETCH_END_USER_IDENTITY)
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
        final String baseUrl = cmcicAgentConfig.getBaseUrl();
        final String basePath = cmcicAgentConfig.getBasePath();
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

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries() {
        return getTrustedBeneficiaries(BENEFICIARIES_PATH);
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(String path) {
        final String baseUrl = cmcicAgentConfig.getBaseUrl();
        final String basePath = cmcicAgentConfig.getBasePath();

        try {
            final HttpResponse response =
                    createAispRequestInSession(baseUrl, basePath + BASE_API_PATH + path)
                            .get(HttpResponse.class);
            if (HttpStatus.SC_NO_CONTENT == response.getStatus()) {
                return Optional.empty();
            }
            return Optional.of(response.getBody(TrustedBeneficiariesResponseDto.class));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }
}
