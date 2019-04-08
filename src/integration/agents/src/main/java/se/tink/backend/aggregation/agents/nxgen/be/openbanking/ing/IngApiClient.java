package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.Urls;
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
import tink.org.apache.http.client.utils.DateUtils;

public final class IngApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final String market;

    IngApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            String market) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.market = market;
    }

    public FetchAccountsResponse fetchAccounts() {
        return buildRequestWithSignature(Urls.ACCOUNTS, Signature.HTTP_METHOD_GET, FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(final AccountEntity account) {
        return buildRequestWithSignature(
                        account.getBalancesUrl(), Signature.HTTP_METHOD_GET, FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchBalancesResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            final String reqPath, final Date fromDate, final Date toDate) {
        final String completeReqPath =
                new URL(reqPath)
                        .queryParam(
                                QueryKeys.DATE_FROM,
                                DateUtils.formatDate(fromDate, QueryValues.DATE_FORMAT))
                        .queryParam(
                                QueryKeys.DATE_TO,
                                DateUtils.formatDate(toDate, QueryValues.DATE_FORMAT))
                        .toString();

        return fetchTransactions(completeReqPath);
    }

    public FetchTransactionsResponse fetchTransactions(final String reqPath) {
        return buildRequestWithSignature(reqPath, Signature.HTTP_METHOD_GET, FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(final String state) {
        System.out.println(state);
        final TokenResponse applicationTokenResponse = getApplicationAccessToken();

        setApplicationTokenToSession(applicationTokenResponse.toTinkToken());

        return new URL(getAuthorizationUrl(applicationTokenResponse).getLocation());
    }

    public OAuth2Token getToken(final String code) {
        final String payload =
                new CustomerTokenRequest(code, persistentStorage.get(StorageKeys.REDIRECT_URL))
                        .toData();

        return fetchToken(payload);
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final String payload = new RefreshTokenRequest(refreshToken).toData();

        return fetchToken(payload);
    }

    public void setTokenToSession(final OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.TOKEN, accessToken);
    }

    private TokenResponse getApplicationAccessToken() {
        final String reqId = IngUtils.getRequestId();
        final String date = getFormattedDate();
        final String payload = new ApplicationTokenRequest().toData();
        final String digest = generateDigest(payload);

        return buildRequest(reqId, date, digest, Urls.TOKEN)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        Signature.SIGNATURE
                                + " "
                                + getAuthorization(
                                        Signature.HTTP_METHOD_POST,
                                        Urls.TOKEN,
                                        reqId,
                                        date,
                                        digest))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, getCertificate())
                .post(TokenResponse.class, payload);
    }

    private AuthorizationUrl getAuthorizationUrl(final TokenResponse tokenResponse) {
        final String reqPath =
                new URL(Urls.OAUTH)
                        .queryParam(
                                QueryKeys.REDIRECT_URI,
                                persistentStorage.get(StorageKeys.REDIRECT_URL))
                        .queryParam(QueryKeys.SCOPE, tokenResponse.getScope())
                        .queryParam(QueryKeys.COUNTRY_CODE, market)
                        .toString();

        return buildRequestWithSignature(reqPath, Signature.HTTP_METHOD_GET, FormValues.EMPTY)
                .addBearerToken(tokenResponse.toTinkToken())
                .get(AuthorizationUrl.class);
    }

    private OAuth2Token fetchToken(final String payload) {
        return buildRequestWithSignature(Urls.TOKEN, Signature.HTTP_METHOD_POST, payload)
                .addBearerToken(getApplicationTokenFromSession())
                .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    private RequestBuilder buildRequestWithSignature(
            final String reqPath, final String httpMethod, final String payload) {
        final String reqId = IngUtils.getRequestId();
        final String date = getFormattedDate();
        final String digest = generateDigest(payload);

        return buildRequest(reqId, date, digest, reqPath)
                .header(
                        HeaderKeys.SIGNATURE,
                        getAuthorization(httpMethod, reqPath, reqId, date, digest));
    }

    private RequestBuilder buildRequest(
            final String reqId, final String date, final String digest, final String reqPath) {
        return client.request(new URL(persistentStorage.get(StorageKeys.BASE_URL) + reqPath))
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.X_ING_REQUEST_ID, reqId);
    }

    private OAuth2Token getApplicationTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.APPLICATION_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find application token!"));
    }

    private void setApplicationTokenToSession(OAuth2Token token) {
        sessionStorage.put(StorageKeys.APPLICATION_TOKEN, token);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    private String getCertificate() {
        return new String(
                IngUtils.readFile(
                        persistentStorage.get(StorageKeys.CLIENT_SIGNING_CERTIFICATE_PATH)));
    }

    private String getAuthorization(
            final String httpMethod,
            final String reqPath,
            final String xIngRequestId,
            final String date,
            final String digest) {
        return new AuthorizationEntity(
                        persistentStorage.get(StorageKeys.CLIENT_ID),
                        getSignature(httpMethod, reqPath, xIngRequestId, date, digest))
                .toString();
    }

    private String getSignature(
            final String httpMethod,
            final String reqPath,
            final String xIngRequestId,
            final String date,
            final String digest) {
        final SignatureEntity signatureEntity =
                new SignatureEntity(httpMethod, reqPath, date, digest, xIngRequestId);

        return IngUtils.generateSignature(
                signatureEntity.toString(),
                persistentStorage.get(StorageKeys.CLIENT_SIGNING_KEY_PATH),
                Signature.SIGNING_ALGORITHM);
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + IngUtils.calculateDigest(data);
    }

    private String getFormattedDate() {
        return IngUtils.getFormattedCurrentDate(Signature.DATE_FORMAT, Signature.TIMEZONE);
    }
}
