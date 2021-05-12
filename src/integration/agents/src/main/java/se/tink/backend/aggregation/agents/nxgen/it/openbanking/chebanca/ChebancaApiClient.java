package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.AutoTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.ConfirmConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.ChebancaRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.TransactionRequestURLBuilder;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConfirmConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.ConsentRequest;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class ChebancaApiClient {
    private final PersistentStorage persistentStorage;
    private final StrongAuthenticationState strongAuthenticationState;
    private final String redirectUrl;
    private final boolean isUserPresent;
    private final ChebancaRequestBuilder chebancaRequestBuilder;

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
                        new URL(redirectUrl)
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

    public HttpResponse getTransactions(
            String accountId,
            Date fromDate,
            Date toDate,
            Long nextAccountingIdx,
            Long nextNotAccountingIdx) {
        return createRequestWithBearerToken(
                        buildTransactionRequestUrl(
                                accountId,
                                fromDate,
                                toDate,
                                nextAccountingIdx,
                                nextNotAccountingIdx),
                        null,
                        HeaderKeys.GET_METHOD)
                .get(HttpResponse.class);
    }

    public void save(String key, Object value) {
        persistentStorage.put(key, value);
    }

    private OAuth2Token getTokenFromStorage() {
        String storageKey = isUserPresent ? StorageKeys.OAUTH_TOKEN : StorageKeys.AUTO_OAUTH_TOKEN;
        return persistentStorage
                .get(storageKey, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    private RequestBuilder createRequestWithBearerToken(
            URL url, String requestBody, String httpMethod) {
        return chebancaRequestBuilder
                .buildRequest(url, requestBody, httpMethod)
                .addBearerToken(getTokenFromStorage());
    }

    private URL buildTransactionRequestUrl(
            String accountId,
            Date fromDate,
            Date toDate,
            Long nextAccountingIdx,
            Long nextNotAccountingIdx) {
        return TransactionRequestURLBuilder.buildTransactionRequestUrl(
                persistentStorage.get(StorageKeys.CUSTOMER_ID),
                accountId,
                fromDate,
                toDate,
                nextAccountingIdx,
                nextNotAccountingIdx);
    }

    public HttpResponse createAutoAuthenticationToken(AutoTokenRequest tokenRequest) {
        return chebancaRequestBuilder
                .buildRequest(
                        Urls.TOKEN,
                        SerializationUtils.serializeToString(tokenRequest),
                        HeaderKeys.POST_METHOD)
                .post(HttpResponse.class, tokenRequest);
    }
}
