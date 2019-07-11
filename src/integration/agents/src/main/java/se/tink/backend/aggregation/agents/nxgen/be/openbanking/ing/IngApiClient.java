package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing;

import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.Urls.ACCOUNTS;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.Urls.TOKEN;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.entities.AuthorizationEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.ApplicationTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.AuthorizationUrl;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.CustomerTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.configuration.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.utils.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import tink.org.apache.http.client.utils.DateUtils;

public class IngApiClient {

    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    private final String market;
    private IngConfiguration configuration;

    public IngApiClient(TinkHttpClient client, SessionStorage sessionStorage, String market) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.market = market;
    }

    public IngConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        IngConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(IngConfiguration configuration) {
        this.configuration = configuration;
    }

    public FetchAccountsResponse fetchAccounts() {
        return buildRequestWithSignature(
                        ACCOUNTS,
                        IngConstants.Signature.HTTP_METHOD_GET,
                        IngConstants.FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(final AccountEntity account) {

        // TODO - Temporary fix due to Sandbox bugs
        String balanceUrl =
                account.getBalancesUrl()
                        .replaceAll("/v3/", "/v3/accounts/")
                        .replaceAll("currency=EUR", "balanceType=expected&currency=EUR");

        return buildRequestWithSignature(
                        balanceUrl,
                        IngConstants.Signature.HTTP_METHOD_GET,
                        IngConstants.FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchBalancesResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            final String reqPath, final Date fromDate, final Date toDate) {

        final String completeReqPath =
                new URL(reqPath)
                        .queryParam(
                                IngConstants.QueryKeys.DATE_FROM,
                                DateUtils.formatDate(
                                        fromDate, IngConstants.QueryValues.DATE_FORMAT))
                        .queryParam(
                                IngConstants.QueryKeys.DATE_TO,
                                DateUtils.formatDate(toDate, IngConstants.QueryValues.DATE_FORMAT))
                        .queryParam(
                                IngConstants.QueryKeys.LIMIT,
                                "10") // TODO - Temporary added for Sandbox specification
                        .toString();
        return fetchTransactions(completeReqPath);
    }

    public FetchTransactionsResponse fetchTransactions(String reqPath) {

        // TODO - Temporary fix for Sandbnox API
        String transactionUrl =
                reqPath.replaceAll("/v2/", "/v2/accounts/")
                        .replaceAll(
                                "currency=EUR&dateFrom=2016-10-01&dateTo=2016-11-21&limit=10",
                                "dateFrom=2018-05-15&dateTo=2018-05-16&limit=10&currency=EUR");

        // TODO - Temporary fix for Sandbnox API
        transactionUrl =
                transactionUrl.substring(0, 13)
                        + "5d8af012-89f0-4c1e-85d1-67d1adb45a04"
                        + transactionUrl.substring(49);

        return buildRequestWithSignature(
                        transactionUrl,
                        IngConstants.Signature.HTTP_METHOD_GET,
                        IngConstants.FormValues.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(final String state) {
        final TokenResponse tokenResponse = getApplicationAccessToken();

        setApplicationTokenToSession(tokenResponse.toTinkToken());
        setClientIdToSession(tokenResponse.getClientId());

        return new URL(getAuthorizationUrl(tokenResponse).getLocation())
                .queryParam(IngConstants.QueryKeys.CLIENT_ID, tokenResponse.getClientId())
                .queryParam(IngConstants.QueryKeys.SCOPE, tokenResponse.getScope())
                .queryParam(IngConstants.QueryKeys.STATE, state)
                .queryParam(
                        IngConstants.QueryKeys.REDIRECT_URI, getConfiguration().getRedirectUrl())
                .queryParam(IngConstants.QueryKeys.RESPONSE_TYPE, IngConstants.QueryValues.CODE);
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
        sessionStorage.put(IngConstants.StorageKeys.TOKEN, accessToken);
    }

    private TokenResponse getApplicationAccessToken() {
        final String reqId = IngUtils.getRequestId();
        final String date = getFormattedDate();
        final String payload = new ApplicationTokenRequest().toData();
        final String digest = generateDigest(payload);

        final String authHeader =
                IngConstants.Signature.SIGNATURE
                        + " "
                        + getAuthorization(
                                getConfiguration().getClientCertificateId(),
                                IngConstants.Signature.HTTP_METHOD_POST,
                                TOKEN,
                                reqId,
                                date,
                                digest);

        return buildRequest(reqId, date, digest, TOKEN)
                .header(IngConstants.HeaderKeys.AUTHORIZATION, authHeader)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(IngConstants.HeaderKeys.TPP_SIGNATURE_CERTIFICATE, getCertificate())
                .post(TokenResponse.class, payload);
    }

    private AuthorizationUrl getAuthorizationUrl(final TokenResponse tokenResponse) {
        final String redirectUrl = getConfiguration().getRedirectUrl();

        final String reqPath =
                new URL(Urls.OAUTH)
                        .queryParam(IngConstants.QueryKeys.REDIRECT_URI, redirectUrl)
                        .queryParam(IngConstants.QueryKeys.SCOPE, tokenResponse.getScope())
                        .queryParam(IngConstants.QueryKeys.COUNTRY_CODE, market)
                        .toString();

        return buildRequestWithSignature(
                        reqPath,
                        IngConstants.Signature.HTTP_METHOD_GET,
                        IngConstants.FormValues.EMPTY)
                .addBearerToken(tokenResponse.toTinkToken())
                .get(AuthorizationUrl.class);
    }

    private OAuth2Token fetchToken(final String payload) {
        return buildRequestWithSignature(TOKEN, IngConstants.Signature.HTTP_METHOD_POST, payload)
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
                        IngConstants.HeaderKeys.SIGNATURE,
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

        return client.request(Urls.BASE_URL + reqPath)
                .accept(MediaType.APPLICATION_JSON)
                .header(IngConstants.HeaderKeys.DIGEST, digest)
                .header(IngConstants.HeaderKeys.DATE, date)
                .header(IngConstants.HeaderKeys.X_ING_REQUEST_ID, reqId);
    }

    private OAuth2Token getApplicationTokenFromSession() {
        return sessionStorage
                .get(IngConstants.StorageKeys.APPLICATION_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(IngConstants.ErrorMessages.MISSING_TOKEN));
    }

    private void setApplicationTokenToSession(OAuth2Token token) {
        sessionStorage.put(IngConstants.StorageKeys.APPLICATION_TOKEN, token);
    }

    private void setClientIdToSession(final String clientId) {
        sessionStorage.put(IngConstants.StorageKeys.CLIENT_ID, clientId);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(IngConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(IngConstants.ErrorMessages.MISSING_TOKEN));
    }

    private String getClientIdFromSession() {
        return sessionStorage
                .get(IngConstants.StorageKeys.CLIENT_ID, String.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        IngConstants.ErrorMessages.MISSING_CLIENT_ID));
    }

    private String getCertificate() {
        return new String(
                BerlinGroupUtils.readFile(getConfiguration().getClientSigningCertificatePath()));
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

        final String clientSigningKey =
                new String(BerlinGroupUtils.readFile(getConfiguration().getClientSigningKeyPath()));

        final SignatureEntity signatureEntity =
                new SignatureEntity(httpMethod, reqPath, date, digest, xIngRequestId);

        return IngUtils.generateSignature(
                signatureEntity.toString(),
                clientSigningKey,
                IngConstants.Signature.SIGNING_ALGORITHM);
    }

    private String generateDigest(final String data) {
        return IngConstants.Signature.DIGEST_PREFIX + IngUtils.calculateDigest(data);
    }

    private String getFormattedDate() {
        return IngUtils.getFormattedCurrentDate(
                IngConstants.Signature.DATE_FORMAT, IngConstants.Signature.TIMEZONE);
    }

    public void authenticate() {

        final TokenResponse tokenResponse = getApplicationAccessToken();

        setApplicationTokenToSession(tokenResponse.toTinkToken());
        setClientIdToSession(tokenResponse.getClientId());
    }
}
