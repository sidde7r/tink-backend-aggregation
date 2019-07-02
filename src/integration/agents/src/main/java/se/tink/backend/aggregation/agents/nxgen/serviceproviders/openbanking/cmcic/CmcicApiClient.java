package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.GrantTypeEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.authenticator.entity.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.util.CodeChallengeUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.util.SignatureUtil;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class CmcicApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private CmcicConfiguration configuration;

    public CmcicApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    private CmcicConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(CmcicConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL baseUrl, String path) {
        final OAuth2Token authToken = getTokenFromStorage();

        final String date = SignatureUtil.getServerTime();

        final String digest = SignatureUtil.generateDigest(FormValues.EMPTY);
        final String requestId = UUID.randomUUID().toString();

        final String signatureValue =
                SignatureUtil.getSignatureHeaderValue(
                        getConfiguration().getKeyId(),
                        Signature.HTTP_METHOD_GET,
                        path,
                        date,
                        digest,
                        requestId,
                        configuration.getClientSigningKeyPath());

        URL requestUrl = baseUrl.concat(path);
        return client.request(requestUrl)
                .addBearerToken(authToken)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.SIGNATURE, signatureValue)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.X_REQUEST_ID, requestId);
    }

    private OAuth2Token getTokenFromStorage() {
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

    public OAuth2Token getToken(String code) {
        String baseUrl = getConfiguration().getBaseUrl();
        String basePath = getConfiguration().getBasePath();
        final URL baseApiUrl = new URL(baseUrl + basePath);

        final URL tokenUrl = baseApiUrl.concat(Urls.TOKEN_PATH);

        final AuthorizationCodeTokenRequest request =
                new AuthorizationCodeTokenRequest(
                        configuration.getClientId(),
                        GrantTypeEnum.CODE,
                        code,
                        configuration.getRedirectUrl(),
                        sessionStorage.get(StorageKeys.CODE_VERIFIER));

        final TokenResponse tokenResponse =
                createRequest(tokenUrl)
                        .body(request.toData(), MediaType.APPLICATION_FORM_URLENCODED)
                        .post(TokenResponse.class);

        return OAuth2Token.create(
                tokenResponse.getTokenType().toString(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn());
    }

    public FetchAccountsResponse fetchAccounts() {
        String baseUrl = getConfiguration().getBaseUrl();
        String basePath = getConfiguration().getBasePath();

        return createRequestInSession(new URL(baseUrl), basePath + Urls.FETCH_ACCOUNTS_PATH)
                .get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, URL nextUrl) {
        URL baseUrl = new URL(getConfiguration().getBaseUrl());
        String basePath = getConfiguration().getBasePath();

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
                createRequestInSession(baseUrl, path).get(FetchTransactionsResponse.class);

        fetchTransactionsResponse.setTransactionalAccount(account);

        return fetchTransactionsResponse;
    }
}
