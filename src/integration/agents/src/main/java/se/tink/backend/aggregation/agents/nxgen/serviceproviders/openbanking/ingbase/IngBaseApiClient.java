package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.ApplicationTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.AuthorizationUrl;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.CustomerTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.IngBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.utils.IngBaseUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IngBaseApiClient {

    private final SimpleDateFormat dateFormat;
    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    private final String market;
    private IngBaseConfiguration configuration;

    public IngBaseApiClient(TinkHttpClient client, SessionStorage sessionStorage, String market) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.market = market;
        dateFormat = new SimpleDateFormat(QueryValues.DATE_FORMAT);
    }

    public IngBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(IngBaseConfiguration configuration) {
        this.configuration = configuration;
    }

    public FetchAccountsResponse fetchAccounts() {
        return buildRequestWithSignature(Urls.ACCOUNTS, Signature.HTTP_METHOD_GET, FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(final AccountEntity account) {

        // TODO - Temporary fix: To replace a query in the URL set by Sandbox API
        String balanceUrl =
                account.getBalancesUrl().replaceAll("currency=EUR", "balanceType=expected");

        return buildRequestWithSignature(balanceUrl, Signature.HTTP_METHOD_GET, FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchBalancesResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            final String reqPath, final Date fromDate, final Date toDate) {
        final String completeReqPath =
                new URL(reqPath)
                        .queryParam(QueryKeys.DATE_FROM, dateFormat.format(fromDate))
                        .queryParam(QueryKeys.DATE_TO, dateFormat.format(fromDate))
                        .queryParam(
                                QueryKeys.LIMIT,
                                "10") // TODO - Temporary added for Sandbox specification
                        .toString();
        return fetchTransactions(completeReqPath);
    }

    public FetchTransactionsResponse fetchTransactions(String reqPath) {

        // TODO - Temporary fix for Sandbnox API
        String transactionUrl = reqPath.replaceAll("currency=EUR&", "");

        return buildRequestWithSignature(
                        transactionUrl, Signature.HTTP_METHOD_GET, FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(final String state) {
        final TokenResponse tokenResponse = getApplicationAccessToken();

        setApplicationTokenToSession(tokenResponse.toTinkToken());
        setClientIdToSession(tokenResponse.getClientId());

        return new URL(getAuthorizationUrl(tokenResponse).getLocation())
                .queryParam(QueryKeys.CLIENT_ID, tokenResponse.getClientId())
                .queryParam(QueryKeys.SCOPE, tokenResponse.getScope())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, getConfiguration().getRedirectUrl())
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE);
    }

    public OAuth2Token getToken(final String code) {
        final String redirectUrl = getConfiguration().getRedirectUrl();

        final String payload = new CustomerTokenRequest(code, redirectUrl).toData();

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
        final String reqId = IngBaseUtils.getRequestId();
        final String date = getFormattedDate();
        final String payload = new ApplicationTokenRequest().toData();
        final String digest = generateDigest(payload);

        final String authHeader =
                Signature.SIGNATURE
                        + " "
                        + getAuthorization(
                                getConfiguration().getClientId(),
                                Signature.HTTP_METHOD_POST,
                                Urls.TOKEN,
                                reqId,
                                date,
                                digest);

        return buildRequest(reqId, date, digest, Urls.TOKEN)
                .header(HeaderKeys.AUTHORIZATION, authHeader)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, getCertificate())
                .post(TokenResponse.class, payload);
    }

    private AuthorizationUrl getAuthorizationUrl(final TokenResponse tokenResponse) {
        final String redirectUrl = getConfiguration().getRedirectUrl();

        final String reqPath =
                new URL(Urls.OAUTH)
                        .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
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
        final String reqId = IngBaseUtils.getRequestId();
        final String date = getFormattedDate();
        final String digest = generateDigest(payload);

        return buildRequest(reqId, date, digest, reqPath)
                .header(
                        HeaderKeys.SIGNATURE,
                        getAuthorization(
                                getClientIdFromSession(),
                                httpMethod,
                                reqPath,
                                reqId,
                                date,
                                digest));
    }

    private RequestBuilder buildRequest(
            final String reqId, final String date, final String digest, final String reqPath) {
        final String baseUrl = getConfiguration().getBaseUrl();

        return client.request(baseUrl + reqPath)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.X_ING_REQUEST_ID, reqId);
    }

    private OAuth2Token getApplicationTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.APPLICATION_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    private void setApplicationTokenToSession(OAuth2Token token) {
        sessionStorage.put(StorageKeys.APPLICATION_TOKEN, token);
    }

    private void setClientIdToSession(final String clientId) {
        sessionStorage.put(StorageKeys.CLIENT_ID, clientId);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    private String getClientIdFromSession() {
        return sessionStorage
                .get(StorageKeys.CLIENT_ID, String.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CLIENT_ID));
    }

    private String getCertificate() {
        return getConfiguration().getClientSigningCertificate();
    }

    private String getAuthorization(
            final String clientId,
            final String httpMethod,
            final String reqPath,
            final String xIngRequestId,
            final String date,
            final String digest) {
        return new AuthorizationEntity(
                        clientId, getSignature(httpMethod, reqPath, xIngRequestId, date, digest))
                .toString();
    }

    private String getSignature(
            final String httpMethod,
            final String reqPath,
            final String xIngRequestId,
            final String date,
            final String digest) {
        final String clientSigningKey = getConfiguration().getClientSigningKey();

        final SignatureEntity signatureEntity =
                new SignatureEntity(httpMethod, reqPath, date, digest, xIngRequestId);

        return IngBaseUtils.generateSignature(
                signatureEntity.toString(), clientSigningKey, Signature.SIGNING_ALGORITHM);
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + IngBaseUtils.calculateDigest(data);
    }

    private String getFormattedDate() {
        return IngBaseUtils.getFormattedCurrentDate(Signature.DATE_FORMAT, Signature.TIMEZONE);
    }
}
