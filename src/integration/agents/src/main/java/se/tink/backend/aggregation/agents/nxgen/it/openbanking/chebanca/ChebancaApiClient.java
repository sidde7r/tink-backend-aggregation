package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.HeadersToSign;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConfirmConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.CustomerIdResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class ChebancaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private ChebancaConfiguration chebancaConfiguration;
    private AgentsServiceConfiguration config;
    private EidasIdentity eidasIdentity;

    public ChebancaApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            StrongAuthenticationState strongAuthenticationState) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    private ChebancaConfiguration getConfiguration() {
        return Optional.ofNullable(chebancaConfiguration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(
            ChebancaConfiguration chebancaConfiguration,
            final AgentsServiceConfiguration configuration,
            EidasIdentity eidasIdentity) {
        this.chebancaConfiguration = chebancaConfiguration;
        this.config = configuration;
        this.eidasIdentity = eidasIdentity;
    }

    private RequestBuilder createRequest(URL url, String requestBody, String httpMethod) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss 'GMT'");
        String date = sdf.format(new Date());
        String requestId = UUID.randomUUID().toString();
        RequestBuilder requestBuilder = client.request(url);
        String digest = null;
        if (requestBody != null) {
            digest = createDigest(requestBody);
            requestBuilder.header(HeaderKeys.DIGEST, digest);
        }
        Map<String, Object> headers = getSigningHeaders(requestId, digest, date, httpMethod, url);

        requestBuilder
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.TPP_REQUEST_ID, requestId)
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.SIGNATURE, generateSignatureHeader(headers));

        return requestBuilder;
    }

    private RequestBuilder createRequestInSession(URL url, String requestBody, String httpMethod) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url, requestBody, httpMethod).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL getAuthorisation() {
        final URL authorizationUrl = buildAuthorizationUrl();
        HttpResponse res =
                createRequest(authorizationUrl, null, HeaderKeys.GET_METHOD)
                        .get(HttpResponse.class);

        return new URL(res.getHeaders().get(HeaderKeys.LOCATION).get(0));
    }

    private URL buildAuthorizationUrl() {
        return Urls.AUTHORIZE
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.CLIENT_ID, chebancaConfiguration.getClientId())
                .queryParam(QueryKeys.REDIRECT_URI, chebancaConfiguration.getRedirectUrl())
                .queryParam(QueryKeys.STATE, strongAuthenticationState.getState());
    }

    public OAuth2Token createToken(TokenRequest tokenRequest) {
        return createRequest(
                        Urls.TOKEN,
                        SerializationUtils.serializeToString(tokenRequest),
                        HeaderKeys.POST_METHOD)
                .post(TokenResponse.class, tokenRequest)
                .toTinkToken();
    }

    private Map<String, Object> getSigningHeaders(
            String requestId, String digest, String date, String httpMethod, URL url) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(QueryKeys.REQUEST_TARGET, formatMethodAndUrl(httpMethod, url));
        Optional.ofNullable(digest).ifPresent(d -> headers.put(HeaderKeys.DIGEST, d));
        headers.put(HeaderKeys.DATE, date);
        headers.put(HeaderKeys.TPP_REQUEST_ID, requestId);

        return headers;
    }

    private String formatMethodAndUrl(String httpMethod, URL url) {
        return String.format("%s %s", httpMethod, url.get().replace(Urls.BASE_URL, ""));
    }

    private String generateSignatureHeader(Map<String, Object> headers) {
        QsealcSigner signer =
                QsealcSigner.build(
                        config.getEidasProxy().toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity,
                        chebancaConfiguration.getCertificateId());

        String signedHeaders =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(String::toLowerCase)
                        .collect(Collectors.joining(" "));

        String signedHeadersWithValues =
                Arrays.stream(HeadersToSign.values())
                        .map(HeadersToSign::getHeader)
                        .filter(headers::containsKey)
                        .map(
                                header ->
                                        String.format(
                                                "%s: %s",
                                                header.toLowerCase(), headers.get(header)))
                        .collect(Collectors.joining("\n"));

        String signature = signer.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(
                HeaderValues.SIGNATURE_HEADER,
                chebancaConfiguration.getApplicationId(),
                signedHeaders,
                signature);
    }

    private String createDigest(String body) {
        return String.format(
                HeaderValues.SHA_256.concat("%s"),
                Base64.getEncoder().encodeToString(Hash.sha256(body)));
    }

    public CustomerIdResponse getCustomerId() {
        return createRequestInSession(Urls.CUSTOMER_ID, null, HeaderKeys.GET_METHOD)
                .get(CustomerIdResponse.class);
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(
                        Urls.ACCOUNTS.parameter(
                                IdTags.CUSTOMER_ID, persistentStorage.get(StorageKeys.CUSTOMER_ID)),
                        null,
                        HeaderKeys.GET_METHOD)
                .get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String accountId) {
        return createRequestInSession(
                        Urls.BALANCES
                                .parameter(
                                        IdTags.CUSTOMER_ID,
                                        persistentStorage.get(StorageKeys.CUSTOMER_ID))
                                .parameter(IdTags.PRODUCT_ID, accountId),
                        null,
                        HeaderKeys.GET_METHOD)
                .get(GetBalancesResponse.class);
    }

    public ConsentResponse createConsent(ConsentRequest consentRequest) {
        return createRequestInSession(
                        Urls.CONSENT.parameter(
                                IdTags.CUSTOMER_ID, persistentStorage.get(StorageKeys.CUSTOMER_ID)),
                        SerializationUtils.serializeToString(consentRequest),
                        HeaderKeys.POST_METHOD)
                .post(ConsentResponse.class, consentRequest);
    }

    public ConsentAuthorizationResponse consentAuthorization(String resourceId) {
        return createRequestInSession(
                        Urls.CONSENT_AUTHORIZATION.parameter(IdTags.RESOURCE_ID, resourceId),
                        null,
                        HeaderKeys.GET_METHOD)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(chebancaConfiguration.getRedirectUrl())
                                .queryParam(QueryKeys.STATE, strongAuthenticationState.getState())
                                .get())
                .get(ConsentAuthorizationResponse.class);
    }

    public void confirmConsent(String resourceId) {
        createRequestInSession(
                        Urls.CONSENT_CONFIRMATION
                                .parameter(IdTags.RESOURCE_ID, resourceId)
                                .parameter(
                                        IdTags.CUSTOMER_ID,
                                        persistentStorage.get(StorageKeys.CUSTOMER_ID)),
                        "{}",
                        HeaderKeys.PUT_METHOD)
                .put(HttpResponse.class, new ConfirmConsentRequest());
    }

    public GetTransactionsResponse getTransactions(String accountId, Date fromDate, Date toDate) {
        return createRequestInSession(
                        buildTransactionRequestUrl(accountId, fromDate, toDate),
                        null,
                        HeaderKeys.GET_METHOD)
                .get(GetTransactionsResponse.class);
    }

    private URL buildTransactionRequestUrl(String accountId, Date fromDate, Date toDate) {
        return Urls.TRANSACTIONS
                .parameter(IdTags.CUSTOMER_ID, persistentStorage.get(StorageKeys.CUSTOMER_ID))
                .parameter(IdTags.PRODUCT_ID, accountId)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate));
    }
}
