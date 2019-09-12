package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.ErrorMessages;
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
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class IngBaseApiClient {

    private final SimpleDateFormat dateFormat;
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    private final String market;
    private EidasIdentity eidasIdentity;
    private IngBaseConfiguration configuration;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private final boolean manualRequest;

    public IngBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String market,
            boolean manualRequest) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.market = market;
        this.manualRequest = manualRequest;
        dateFormat = new SimpleDateFormat(QueryValues.DATE_FORMAT);
    }

    public IngBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(
            IngBaseConfiguration configuration,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity) {
        this.configuration = configuration;
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
    }

    public FetchAccountsResponse fetchAccounts() {
        return buildRequestWithSignature(
                        Urls.ACCOUNTS, Signature.HTTP_METHOD_GET, StringUtils.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(final AccountEntity account) {
        String balanceUrl = account.getBalancesUrl();
        return buildRequestWithSignature(balanceUrl, Signature.HTTP_METHOD_GET, StringUtils.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchBalancesResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(final String transactionsUrl) {
        final String completeReqPath =
                new URL(transactionsUrl)
                        .queryParam(IngBaseConstants.QueryKeys.DATE_FROM, getTransactionsDateFrom())
                        .queryParam(
                                IngBaseConstants.QueryKeys.DATE_TO, dateFormat.format(new Date()))
                        .queryParam(IngBaseConstants.QueryKeys.LIMIT, "100")
                        .toString();
        return buildRequestWithSignature(
                        completeReqPath, Signature.HTTP_METHOD_GET, StringUtils.EMPTY)
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
                .queryParam(
                        QueryKeys.SCOPE,
                        QueryValues.PAYMENT_ACCOUNTS_TRANSACTIONS_AND_BALANCES_VIEW)
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
        persistentStorage.put(StorageKeys.TOKEN, accessToken);
    }

    private TokenResponse getApplicationAccessToken() {
        final String reqId = IngBaseUtils.getRequestId();
        final String date = getFormattedDate();

        /*
           ING According to documentation expects here grant_type with scope
           grant_type=client_credentials&scope=<scope of the token>
           however even if it accepts the scope, it returns full scope of the token
           which we actually can handle that by passing hardcoded (allowed for us scope) later in the flow
           IngBaseConstants.PAYMENT_ACCOUNTS_TRANSACTIONS_AND_BALANCES_VIEW
           After fix on their side we can use scope returned by token to have more elastic solution.
        */
        final String payload = new ApplicationTokenRequest().toData();
        final String digest = generateDigest(payload);

        final String authHeader =
                Signature.SIGNATURE
                        + StringUtils.SPACE
                        + getAuthorization(
                                getConfiguration().getClientCertificateSerial(),
                                Signature.HTTP_METHOD_POST,
                                Urls.TOKEN,
                                reqId,
                                date,
                                digest);

        return buildRequest(reqId, date, digest, Urls.TOKEN)
                .header(HeaderKeys.AUTHORIZATION, authHeader)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                        HeaderKeys.TPP_SIGNATURE_CERTIFICATE,
                        getConfiguration().getClientCertificate())
                .post(TokenResponse.class, payload);
    }

    private String getTransactionsDateFrom() {
        if (manualRequest) {
            return QueryValues.TRANSACTION_FROM_DATE;
        } else {
            return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                    DateUtils.addDays(new Date(), -QueryValues.MAX_PERIOD_IN_DAYS));
        }
    }

    private AuthorizationUrl getAuthorizationUrl(final TokenResponse tokenResponse) {
        final String redirectUrl = getConfiguration().getRedirectUrl();

        final String reqPath =
                new URL(Urls.OAUTH)
                        .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                        .queryParam(
                                QueryKeys.SCOPE,
                                QueryValues.PAYMENT_ACCOUNTS_TRANSACTIONS_AND_BALANCES_VIEW)
                        .queryParam(QueryKeys.COUNTRY_CODE, market)
                        .toString();

        return buildRequestWithSignature(reqPath, Signature.HTTP_METHOD_GET, StringUtils.EMPTY)
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
        return persistentStorage
                .get(StorageKeys.APPLICATION_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    private void setApplicationTokenToSession(OAuth2Token token) {
        persistentStorage.put(StorageKeys.APPLICATION_TOKEN, token);
    }

    private void setClientIdToSession(final String clientId) {
        persistentStorage.put(StorageKeys.CLIENT_ID, clientId);
    }

    private OAuth2Token getTokenFromSession() {
        return persistentStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    private String getClientIdFromSession() {
        return persistentStorage
                .get(StorageKeys.CLIENT_ID, String.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CLIENT_ID));
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

        final SignatureEntity signatureEntity =
                new SignatureEntity(httpMethod, reqPath, date, digest, xIngRequestId);

        QsealcSigner proxySigner =
                QsealcSigner.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity,
                        configuration.getCertificateId());

        return proxySigner.getSignatureBase64(signatureEntity.toString().getBytes());
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + IngBaseUtils.calculateDigest(data);
    }

    private String getFormattedDate() {
        return IngBaseUtils.getFormattedCurrentDate(Signature.DATE_FORMAT, Signature.TIMEZONE);
    }
}
