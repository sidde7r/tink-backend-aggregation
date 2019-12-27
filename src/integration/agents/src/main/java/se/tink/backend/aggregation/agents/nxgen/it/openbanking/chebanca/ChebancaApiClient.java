package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca;

import static java.util.Objects.requireNonNull;

import java.util.Date;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.ChebancaRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.QsealcSignerProvider;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.SignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConfirmConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentRequest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ChebancaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private ChebancaConfiguration chebancaConfig;
    private AgentsServiceConfiguration config;
    private ChebancaRequestBuilder chebancaRequestBuilder;

    public ChebancaApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            StrongAuthenticationState strongAuthenticationState,
            ChebancaConfiguration chebancaConfig,
            final AgentsServiceConfiguration configuration,
            EidasIdentity eidasIdentity) {

        this.client = requireNonNull(client);
        this.persistentStorage = requireNonNull(persistentStorage);
        this.strongAuthenticationState = requireNonNull(strongAuthenticationState);
        this.chebancaConfig = requireNonNull(chebancaConfig);
        this.config = requireNonNull(configuration);
        requireNonNull(eidasIdentity);
        this.chebancaRequestBuilder =
                createChebancaRequestBuilder(client, chebancaConfig, eidasIdentity);
    }

    public HttpResponse getLoginUrl(URL authorizationUrl) { // performs URL redirection
        return chebancaRequestBuilder
                .buildRequest(authorizationUrl, null, HeaderKeys.GET_METHOD)
                .get(HttpResponse.class);
    }

    public HttpResponse createToken(TokenRequest tokenRequest) {
        return chebancaRequestBuilder
                .buildRequest(
                        Urls.TOKEN,
                        SerializationUtils.serializeToString(tokenRequest),
                        HeaderKeys.POST_METHOD)
                .post(HttpResponse.class, tokenRequest);
    }

    public HttpResponse getCustomerId() {
        return createRequestWithBearerToken(Urls.CUSTOMER_ID, null, HeaderKeys.GET_METHOD)
                .get(HttpResponse.class);
    }

    public HttpResponse getAccounts() {
        return createRequestWithBearerToken(
                        Urls.ACCOUNTS.parameter(
                                IdTags.CUSTOMER_ID, persistentStorage.get(StorageKeys.CUSTOMER_ID)),
                        null,
                        HeaderKeys.GET_METHOD)
                .get(HttpResponse.class);
    }

    public HttpResponse getBalances(String accountId) {
        return createRequestWithBearerToken(
                        Urls.BALANCES
                                .parameter(
                                        IdTags.CUSTOMER_ID,
                                        persistentStorage.get(StorageKeys.CUSTOMER_ID))
                                .parameter(IdTags.PRODUCT_ID, accountId),
                        null,
                        HeaderKeys.GET_METHOD)
                .get(HttpResponse.class);
    }

    public HttpResponse createConsent(ConsentRequest consentRequest) {
        return createRequestWithBearerToken(
                        Urls.CONSENT.parameter(
                                IdTags.CUSTOMER_ID, persistentStorage.get(StorageKeys.CUSTOMER_ID)),
                        SerializationUtils.serializeToString(consentRequest),
                        HeaderKeys.POST_METHOD)
                .post(HttpResponse.class, consentRequest);
    }

    public HttpResponse authorizeConsent(String resourceId) {
        return createRequestWithBearerToken(
                        Urls.CONSENT_AUTHORIZATION.parameter(IdTags.RESOURCE_ID, resourceId),
                        null,
                        HeaderKeys.GET_METHOD)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(chebancaConfig.getRedirectUrl())
                                .queryParam(QueryKeys.STATE, strongAuthenticationState.getState())
                                .get())
                .get(HttpResponse.class);
    }

    public HttpResponse confirmConsent(String resourceId) {
        return createRequestWithBearerToken(
                        Urls.CONSENT_CONFIRMATION
                                .parameter(IdTags.RESOURCE_ID, resourceId)
                                .parameter(
                                        IdTags.CUSTOMER_ID,
                                        persistentStorage.get(StorageKeys.CUSTOMER_ID)),
                        "{}",
                        HeaderKeys.PUT_METHOD)
                .put(HttpResponse.class, new ConfirmConsentRequest());
    }

    public HttpResponse getTransactions(String accountId, Date fromDate, Date toDate) {
        return createRequestWithBearerToken(
                        buildTransactionRequestUrl(accountId, fromDate, toDate),
                        null,
                        HeaderKeys.GET_METHOD)
                .get(HttpResponse.class);
    }

    public void save(String key, Object value) {
        persistentStorage.put(key, value);
    }

    private ChebancaRequestBuilder createChebancaRequestBuilder(
            TinkHttpClient client,
            ChebancaConfiguration chebancaConfig,
            EidasIdentity eidasIdentity) {
        return new ChebancaRequestBuilder(
                client,
                new SignatureHeaderGenerator(
                        chebancaConfig.getApplicationId(),
                        QsealcSignerProvider.getQsealcSigner(
                                config.getEidasProxy(),
                                eidasIdentity,
                                chebancaConfig.getCertificateId())));
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    private RequestBuilder createRequestWithBearerToken(
            URL url, String requestBody, String httpMethod) {
        return chebancaRequestBuilder
                .buildRequest(url, requestBody, httpMethod)
                .addBearerToken(getTokenFromStorage());
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
