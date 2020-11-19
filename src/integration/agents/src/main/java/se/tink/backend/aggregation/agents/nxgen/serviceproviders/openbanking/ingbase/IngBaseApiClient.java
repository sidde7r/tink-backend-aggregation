package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import com.google.gson.Gson;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.ErrorCodes;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.MarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.BaseFetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.utils.ProviderSessionCacheController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.DateFormat;

public class IngBaseApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final String market;
    private EidasIdentity eidasIdentity;
    private String redirectUrl;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private String hexCertificateSerial;
    private String base64derQsealc;
    private final ProviderSessionCacheController providerSessionCacheController;
    private final boolean isManualAuthentication;
    private MarketConfiguration marketConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(IngBaseApiClient.class);

    public IngBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String market,
            ProviderSessionCacheController providerSessionCacheController,
            boolean isManualAuthentication,
            MarketConfiguration marketConfiguration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.market = market;
        this.providerSessionCacheController = providerSessionCacheController;
        this.isManualAuthentication = isManualAuthentication;
        this.marketConfiguration = marketConfiguration;
    }

    public void setConfiguration(
            AgentConfiguration<IngBaseConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity)
            throws CertificateException {
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.eidasIdentity = eidasIdentity;
        this.hexCertificateSerial =
                CertificateUtils.getSerialNumber(agentConfiguration.getQsealc(), 16);
        this.base64derQsealc =
                CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                        agentConfiguration.getQsealc());
    }

    public FetchAccountsResponse fetchAccounts() {
        try {
            return buildRequestWithSignature(
                            Urls.ACCOUNTS, Signature.HTTP_METHOD_GET, StringUtils.EMPTY)
                    .addBearerToken(getTokenFromSession())
                    .type(MediaType.APPLICATION_JSON)
                    .get(FetchAccountsResponse.class);
        } catch (HttpResponseException e) {
            ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
            if (errorResponse.getErrorCode().equals(ErrorCodes.NOT_FOUND)) {
                clearToken();
                throw AuthorizationError.UNAUTHORIZED.exception();
            }
            if (e.getResponse().getStatus() == 401) {
                clearToken();
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    public FetchBalancesResponse fetchBalances(final AccountEntity account) {
        String balanceUrl = account.getBalancesUrl();
        return buildRequestWithSignature(balanceUrl, Signature.HTTP_METHOD_GET, StringUtils.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(FetchBalancesResponse.class);
    }

    public BaseFetchTransactionsResponse fetchTransactions(
            final String transactionsUrl, LocalDate fromDate, LocalDate toDate) {
        final String path =
                new URL(transactionsUrl)
                        .queryParam(
                                IngBaseConstants.QueryKeys.DATE_FROM,
                                fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .queryParam(
                                IngBaseConstants.QueryKeys.DATE_TO,
                                toDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .queryParam(
                                IngBaseConstants.QueryKeys.LIMIT, QueryValues.TRANSACTIONS_LIMIT)
                        .toString();
        return fetchTransactionsPage(path);
    }

    public BaseFetchTransactionsResponse fetchTransactionsPage(final String transactionsUrl) {
        return buildRequestWithSignature(
                        transactionsUrl, Signature.HTTP_METHOD_GET, StringUtils.EMPTY)
                .addBearerToken(getTokenFromSession())
                .type(MediaType.APPLICATION_JSON)
                .get(marketConfiguration.getTransactionsResponseClass());
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
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE);
    }

    public OAuth2Token getToken(final String code) {

        final String payload = new CustomerTokenRequest(code, redirectUrl).toData();
        return fetchToken(payload).toTinkToken();
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final TokenResponse tokenResponse = getApplicationAccessToken();
        setApplicationTokenToSession(tokenResponse.toTinkToken());

        final String payload = new RefreshTokenRequest(refreshToken).toData();
        return fetchToken(payload).toTinkToken(getTokenFromSession());
    }

    public void setTokenToSession(final OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.TOKEN, accessToken);
    }

    private TokenResponse getApplicationAccessToken() {
        if (!isManualAuthentication) {
            /*
                Reuse the application access token which saved in the cache if it is still valid
                during auto authentication
            */
            Optional<Map<String, String>> cacheInfoOpt =
                    providerSessionCacheController.getProviderSessionCacheInformation();
            if (cacheInfoOpt.isPresent()) {
                Map<String, String> cacheInfo = cacheInfoOpt.get();
                String applicationToken = cacheInfo.get(StorageKeys.APPLICATION_TOKEN);
                if (applicationToken != null) {
                    try {
                        logger.info("Get application token from cache");
                        final TokenResponse response =
                                new Gson().fromJson(applicationToken, TokenResponse.class);
                        return response;
                    } catch (Exception e) {
                        logger.warn("Unable to parse payload : " + applicationToken);
                    }
                }
            }
        }

        final String reqId = Psd2Headers.getRequestId();
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
                                "SN=" + hexCertificateSerial,
                                Signature.HTTP_METHOD_POST,
                                Urls.TOKEN,
                                reqId,
                                date,
                                digest);

        final TokenResponse response =
                buildRequest(reqId, date, digest, Urls.TOKEN)
                        .header(HeaderKeys.AUTHORIZATION, authHeader)
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, base64derQsealc)
                        .post(TokenResponse.class, payload);

        /*
           Save the valid application access token to cache
           The cache should be expired earlier than the token (1 min)
        */
        String payloadStr = new Gson().toJson(response);
        Map<String, String> applicationTokenMap = new HashMap<>();
        applicationTokenMap.put(StorageKeys.APPLICATION_TOKEN, payloadStr);
        providerSessionCacheController.setProviderSessionCacheInfoWithExpiredTime(
                applicationTokenMap, (int) (response.getExpiresIn() - 60));
        return response;
    }

    private AuthorizationUrl getAuthorizationUrl(final TokenResponse tokenResponse) {

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

    private TokenResponse fetchToken(final String payload) {
        return new CertificateIsRevokedExceptionRequestRepeater(this, payload).execute();
    }

    RequestBuilder buildRequestWithSignature(
            final String reqPath, final String httpMethod, final String payload) {
        final String reqId = Psd2Headers.getRequestId();
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
        return client.request(Urls.BASE_URL + reqPath)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.X_ING_REQUEST_ID, reqId);
    }

    OAuth2Token getApplicationTokenFromSession() {
        return persistentStorage
                .get(StorageKeys.APPLICATION_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }

    private void setApplicationTokenToSession(OAuth2Token token) {
        persistentStorage.put(StorageKeys.APPLICATION_TOKEN, token);
    }

    private void setClientIdToSession(final String clientId) {
        persistentStorage.put(StorageKeys.CLIENT_ID, clientId, false);
    }

    private void clearToken() {
        persistentStorage.remove(StorageKeys.TOKEN);
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
                QsealcSignerImpl.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);

        return proxySigner.getSignatureBase64(signatureEntity.toString().getBytes());
    }

    private String generateDigest(final String data) {
        return Signature.DIGEST_PREFIX + Psd2Headers.calculateDigest(data);
    }

    private String getFormattedDate() {
        return DateFormat.getFormattedCurrentDate(Signature.DATE_FORMAT, Signature.TIMEZONE);
    }
}
