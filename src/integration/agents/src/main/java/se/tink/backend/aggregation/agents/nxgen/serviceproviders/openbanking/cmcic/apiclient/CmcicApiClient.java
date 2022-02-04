package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls.BASE_API_PATH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls.BENEFICIARIES_PATH;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.AuthorizationCodeTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.PisTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.RefreshTokenTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
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
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CmcicApiClient implements FrAispApiClient {

    private static final String EMPTY_BODY = "{}";

    private final TinkHttpClient client;
    private final CmcicRepository cmcicRepository;
    private final CmcicConfiguration configuration;
    private final CmcicDigestProvider digestProvider;
    private final CmcicSignatureProvider signatureProvider;
    private final CmcicCodeChallengeProvider codeChallengeProvider;
    private final CmcicAgentConfig cmcicAgentConfig;
    private final CmcicRequestValuesProvider cmcicRequestValuesProvider;
    private final String redirectUrl;

    public CmcicApiClient(
            TinkHttpClient client,
            CmcicRepository cmcicRepository,
            AgentConfiguration<CmcicConfiguration> agentConfiguration,
            CmcicDigestProvider digestProvider,
            CmcicSignatureProvider signatureProvider,
            CmcicCodeChallengeProvider codeChallengeProvider,
            CmcicAgentConfig cmcicAgentConfig,
            CmcicRequestValuesProvider cmcicRequestValuesProvider) {
        this.client = client;
        this.cmcicRepository = cmcicRepository;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.digestProvider = digestProvider;
        this.signatureProvider = signatureProvider;
        this.codeChallengeProvider = codeChallengeProvider;
        this.cmcicAgentConfig = cmcicAgentConfig;
        this.cmcicRequestValuesProvider = cmcicRequestValuesProvider;
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries() {
        final String basePath = cmcicAgentConfig.getBasePath();
        return getTrustedBeneficiaries(
                String.format("%s%s%s", basePath, BASE_API_PATH, BENEFICIARIES_PATH));
    }

    @Override
    public Optional<TrustedBeneficiariesResponseDto> getTrustedBeneficiaries(String path) {
        final String baseUrl = cmcicAgentConfig.getBaseUrl();

        try {
            final HttpResponse response =
                    createAispRequestInSession(baseUrl, path).get(HttpResponse.class);
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

    public URL getAuthorizePisURL(URL url, String state) {
        return authorizePisRequest(client.request(url), state);
    }

    public URL getAuthorizeUrl(String state) {
        return authorizeRequest(
                client.request(cmcicAgentConfig.getAuthBaseUrl())
                        .queryParam(QueryKeys.STATE, state)
                        .queryParam(QueryKeys.REDIRECT_URI, redirectUrl));
    }

    public EndUserIdentityResponse getEndUserIdentity() {
        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();

        return createAispRequestInSession(baseUrl, basePath + Urls.FETCH_END_USER_IDENTITY)
                .get(EndUserIdentityResponse.class);
    }

    public HalPaymentRequestEntity fetchPayment(String uniqueId) {
        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();

        return createPispRequestInSession(
                        baseUrl, basePath + Urls.PAYMENT_REQUESTS + "/" + uniqueId)
                .type(MediaType.APPLICATION_JSON)
                .get(HalPaymentRequestEntity.class);
    }

    public HalPaymentRequestEntity confirmPayment(String uniqueId) {
        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();
        return createPispRequestInSession(
                        new URL(baseUrl),
                        String.format(
                                "%s%s/%s/%s",
                                basePath,
                                Urls.PAYMENT_REQUESTS,
                                uniqueId,
                                Urls.PIS_CONFIRMATION_PATH),
                        EMPTY_BODY)
                .type(MediaType.APPLICATION_JSON)
                .post(HalPaymentRequestEntity.class, EMPTY_BODY);
    }

    public void fetchPisOauthToken() {
        if (!isValidPisOauthToken()) {
            fetchAndSavePisOauthToken();
        }
    }

    public HalPaymentRequestCreation makePayment(
            PaymentRequestResourceEntity paymentRequestResourceEntity) {

        String baseUrl = cmcicAgentConfig.getBaseUrl();
        String basePath = cmcicAgentConfig.getBasePath();

        String body = SerializationUtils.serializeToString(paymentRequestResourceEntity);

        HttpResponse response =
                createPispRequestInSession(new URL(baseUrl), basePath + Urls.PAYMENT_REQUESTS, body)
                        .type(MediaType.APPLICATION_JSON)
                        .body(body)
                        .post(HttpResponse.class);

        cmcicRepository.storePaymentId(getPaymentId(response.getHeaders().get("location")));

        return response.getBody(HalPaymentRequestCreation.class);
    }

    private String getPaymentId(List<String> headers) {
        if (!headers.isEmpty()) {
            String paymentId = headers.get(0);
            int i = paymentId.lastIndexOf("/");
            return paymentId.substring(i + 1);
        }
        throw new IllegalStateException("No payment id");
    }

    public OAuth2Token exchangeCodeForToken(String code) {
        final AuthorizationCodeTokenRequest request =
                new AuthorizationCodeTokenRequest(
                        configuration.getClientId(),
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        cmcicRepository.getCodeVerifier(),
                        redirectUrl);
        return executeTokenRequest(request);
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        final RefreshTokenTokenRequest request =
                new RefreshTokenTokenRequest(
                        configuration.getClientId(), refreshToken, FormValues.REFRESH_TOKEN);
        return executeTokenRequestAndCheckTokenNotExpired(request);
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

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createPispRequestInSession(String baseUrl, String path) {
        return createRequestInSession(
                baseUrl,
                path,
                cmcicRepository
                        .findPispToken()
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
                cmcicRepository
                        .findPispToken()
                        .filter(OAuth2Token::isValid)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.NO_PIS_OAUTH_TOKEN_IN_STORAGE)));
    }

    private RequestBuilder createAispRequestInSession(String baseUrl, String path) {
        return createRequestInSession(baseUrl, path, cmcicRepository.getAispToken());
    }

    private RequestBuilder createRequestInSession(
            URL baseUrl, String path, String body, OAuth2Token authToken) {

        final String date = cmcicRequestValuesProvider.getServerTime();
        final String digest = digestProvider.generateDigest(body);
        final String requestId = cmcicRequestValuesProvider.randomUuid();
        URL requestUrl = baseUrl.concat(path);

        String signatureHeaderValue =
                signatureProvider.getSignatureHeaderValueForPost(
                        configuration.getKeyId(), requestUrl.toUri(), date, digest, requestId);

        return client.request(requestUrl)
                .addBearerToken(authToken)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.USER_AGENT, cmcicRequestValuesProvider.getOrganizationName())
                .header(HeaderKeys.HOST, requestUrl.toUri().getHost())
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.SIGNATURE, signatureHeaderValue)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(
            String baseUrl, String path, OAuth2Token authToken) {

        final String date = cmcicRequestValuesProvider.getServerTime();
        final String requestId = cmcicRequestValuesProvider.randomUuid();
        URL requestUrl = new URL(baseUrl + path);

        String signatureHeaderValue =
                signatureProvider.getSignatureHeaderValueForGet(
                        configuration.getKeyId(), requestUrl.toUri(), date, requestId);

        return client.request(requestUrl)
                .addBearerToken(authToken)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.USER_AGENT, cmcicRequestValuesProvider.getOrganizationName())
                .header(HeaderKeys.HOST, requestUrl.toUri().getHost())
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.SIGNATURE, signatureHeaderValue)
                .header(HeaderKeys.X_REQUEST_ID, requestId);
    }

    private URL authorizeRequest(RequestBuilder request) {
        return request.queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParamRaw(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.CODE_CHALLENGE, getCodeChallenge())
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    private URL authorizePisRequest(RequestBuilder request, String state) {
        return request.queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE_CHALLENGE, getCodeChallenge())
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    private String getCodeChallenge() {
        final String codeVerifier = codeChallengeProvider.generateCodeVerifier();
        cmcicRepository.storeCodeVerifier(codeVerifier);
        return codeChallengeProvider.generateCodeChallengeForCodeVerifier(codeVerifier);
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

    private boolean isValidPisOauthToken() {
        return cmcicRepository.findPispToken().map(OAuth2Token::isValid).orElse(false);
    }

    private void fetchAndSavePisOauthToken() {
        PisTokenRequest pisTokenRequest = new PisTokenRequest(configuration.getClientId());
        OAuth2Token token = executeTokenRequest(pisTokenRequest);
        cmcicRepository.storePispToken(token);
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
}
