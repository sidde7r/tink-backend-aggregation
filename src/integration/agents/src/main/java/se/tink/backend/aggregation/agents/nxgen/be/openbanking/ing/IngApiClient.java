package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.entities.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.ApplicationTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.AuthorizationUrl;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.CustomerTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.utils.IngUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class IngApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final String market;

    public IngApiClient(
        TinkHttpClient client, SessionStorage sessionStorage, PersistentStorage persistentStorage,
        String market) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.market = market;
    }

    public FetchAccountsResponse fetchAccounts() {
        return buildRequestWithSignature(IngConstants.Urls.ACCOUNTS,
            IngConstants.Signature.HTTP_METHOD_GET,
            IngConstants.FormValues.EMPTY)
            .addBearerToken(getTokenFromSession())
            .type(MediaType.APPLICATION_JSON)
            .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(final AccountEntity account) {
        return buildRequestWithSignature(
            account.getBalancesUrl(),
            IngConstants.Signature.HTTP_METHOD_GET,
            IngConstants.FormValues.EMPTY)
            .addBearerToken(getTokenFromSession())
            .type(MediaType.APPLICATION_JSON)
            .get(FetchBalancesResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(final String reqPath, final Date fromDate,
        final Date toDate) {
        final String completeReqPath = new URL(reqPath)
            .queryParam(IngConstants.QueryKeys.DATE_FROM,
                IngUtils.formatDate(fromDate, IngConstants.QueryValues.DATE_FORMAT))
            .queryParam(IngConstants.QueryKeys.DATE_TO,
                IngUtils.formatDate(toDate, IngConstants.QueryValues.DATE_FORMAT)).toString();

        return fetchTransactions(completeReqPath);
    }

    public FetchTransactionsResponse fetchTransactions(final String reqPath) {
        return buildRequestWithSignature(reqPath, IngConstants.Signature.HTTP_METHOD_GET,
            IngConstants.FormValues.EMPTY)
            .addBearerToken(getTokenFromSession())
            .type(MediaType.APPLICATION_JSON)
            .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(final String state) {
        System.out.println(state);
        final TokenResponse applicationTokenResponse = getApplicationAccessToken();

        setApplicationTokenToSession(applicationTokenResponse.getToken());

        return new URL(getAuthorizationUrl(applicationTokenResponse).getLocation());
    }

    public OAuth2Token getToken(final String code) {
        final String payload = new CustomerTokenRequest(code,
            persistentStorage.get(IngConstants.StorageKeys.REDIRECT_URL))
            .toData();

        return fetchToken(payload);
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final String payload = new RefreshTokenRequest(refreshToken)
            .toData();

        return fetchToken(payload);
    }

    public void setTokenToSession(final OAuth2Token accessToken) {
        sessionStorage.put(IngConstants.StorageKeys.TOKEN, accessToken);
    }

    private TokenResponse getApplicationAccessToken() {
        final String reqId = IngUtils.getRequestId();
        final String date = getFormattedDate();
        final String payload = new ApplicationTokenRequest().toData();
        final String digest = generateDigest(payload);

        return buildRequest(reqId, date, digest, IngConstants.Urls.TOKEN)
            .header(IngConstants.HeaderKeys.AUTHORIZATION,
                IngConstants.Signature.SIGNATURE + " " + getAuthorization(
                    IngConstants.Signature.HTTP_METHOD_POST, IngConstants.Urls.TOKEN,
                    reqId, date, digest))
            .type(MediaType.APPLICATION_FORM_URLENCODED)
            .header(IngConstants.HeaderKeys.TPP_SIGNATURE_CERTIFICATE, getCertificate())
            .post(TokenResponse.class, payload);
    }

    private AuthorizationUrl getAuthorizationUrl(final TokenResponse tokenResponse) {

        String reqPath = new URL(IngConstants.Urls.OAUTH)
            .queryParam(IngConstants.QueryKeys.REDIRECT_URI,
                persistentStorage.get(IngConstants.StorageKeys.REDIRECT_URL))
            .queryParam(IngConstants.QueryKeys.SCOPE, tokenResponse.getScope())
            .queryParam(IngConstants.QueryKeys.COUNTRY_CODE, market).toString();

        return buildRequestWithSignature(reqPath, IngConstants.Signature.HTTP_METHOD_GET,
            IngConstants.FormValues.EMPTY)
            .addBearerToken(tokenResponse.getToken())
            .get(AuthorizationUrl.class);
    }

    private OAuth2Token fetchToken(final String payload) {
        return buildRequestWithSignature(IngConstants.Urls.TOKEN,
            IngConstants.Signature.HTTP_METHOD_POST,
            payload)
            .addBearerToken(getApplicationTokenFromSession())
            .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
            .post(TokenResponse.class)
            .getToken();
    }

    private RequestBuilder buildRequestWithSignature(final String reqPath, final String httpMethod,
        final String payload) {
        final String reqId = IngUtils.getRequestId();
        final String date = getFormattedDate();
        final String digest = generateDigest(payload);

        return buildRequest(reqId, date, digest, reqPath)
            .header(IngConstants.HeaderKeys.SIGNATURE,
                getAuthorization(httpMethod, reqPath,
                    reqId, date, digest));
    }

    private RequestBuilder buildRequest(final String reqId, final String date, final String digest,
        final String reqPath) {

        return client
            .request(
                new URL(
                    persistentStorage.get(IngConstants.StorageKeys.BASE_URL)
                        + reqPath))
            .accept(MediaType.APPLICATION_JSON)
            .header(IngConstants.HeaderKeys.DIGEST, digest)
            .header(IngConstants.HeaderKeys.DATE, date)
            .header(IngConstants.HeaderKeys.X_ING_REQUEST_ID, reqId);
    }

    private OAuth2Token getApplicationTokenFromSession() {
        return sessionStorage
            .get(IngConstants.StorageKeys.APPLICATION_TOKEN, OAuth2Token.class)
            .orElseThrow(() -> new IllegalStateException("Cannot find application token!"));
    }

    private void setApplicationTokenToSession(OAuth2Token token) {
        sessionStorage
            .put(IngConstants.StorageKeys.APPLICATION_TOKEN, token);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
            .get(IngConstants.StorageKeys.TOKEN, OAuth2Token.class)
            .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    private String getCertificate() {
        return new String(
            IngUtils.readFile(
                persistentStorage.get(IngConstants.StorageKeys.CLIENT_SIGNING_CERTIFICATE_PATH)));
    }

    private String getAuthorization(final String httpMethod, final String reqPath,
        final String xIngRequestId,
        final String date, final String digest) {
        return new AuthorizationEntity(persistentStorage.get(IngConstants.StorageKeys.CLIENT_ID),
            getSignature(httpMethod, reqPath, xIngRequestId, date, digest)).toString();
    }

    private String getSignature(final String httpMethod, final String reqPath,
        final String xIngRequestId,
        final String date, final String digest) {
        final SignatureEntity signatureEntity = new SignatureEntity(httpMethod, reqPath,
            date, digest, xIngRequestId);

        return IngUtils.generateSignature(
            signatureEntity.toString(),
            persistentStorage.get(IngConstants.StorageKeys.CLIENT_SIGNING_KEY_PATH),
            IngConstants.Signature.SIGNING_ALGORITHM);
    }

    private String generateDigest(final String data) {
        return IngConstants.Signature.DIGEST_PREFIX + IngUtils
            .calculateDigest(data);
    }

    private String getFormattedDate() {
        return IngUtils.getFormattedCurrentDate(IngConstants.Signature.DATE_FORMAT,
            IngConstants.Signature.TIMEZONE);
    }
}