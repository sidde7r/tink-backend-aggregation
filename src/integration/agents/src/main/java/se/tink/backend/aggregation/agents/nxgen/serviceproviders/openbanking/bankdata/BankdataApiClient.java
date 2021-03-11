package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata;

import java.security.cert.CertificateException;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.InitialTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.configuration.BankdataConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.AuthorizePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.BankdataErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.DateFormat;
import se.tink.libraries.payment.enums.PaymentType;

@RequiredArgsConstructor
public class BankdataApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final BankdataApiConfiguration apiConfiguration;

    private BankdataConfiguration configuration;
    private String redirectUrl;
    private String clientId;

    protected void setConfiguration(
            AgentConfiguration<BankdataConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.client.setEidasProxy(eidasProxyConfiguration);
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        try {
            this.clientId =
                    CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());
        } catch (CertificateException e) {
            throw new IllegalStateException("Could not extract organization identifier!", e);
        }
    }

    public URL getAuthorizeUrl(String state) {
        String consentId = getConsentId();
        authorizeConsent(consentId);
        final String codeVerifier = Psd2Headers.generateCodeVerifier();
        final String codeChallenge = Psd2Headers.generateCodeChallenge(codeVerifier);
        persistentStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
        URL url = new URL(apiConfiguration.getBaseAuthUrl() + Endpoints.AUTHORIZE);

        return createRequest(url)
                .queryParam(QueryKeys.RESPONSE_TYPE, BankdataConstants.QueryValues.CODE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE + consentId)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl)
                .queryParam("acr", "psd2")
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final AccessTokenRequest request =
                new AccessTokenRequest(
                        BankdataConstants.FormValues.AUTHORIZATION_CODE,
                        clientId,
                        code,
                        redirectUrl,
                        persistentStorage.get(StorageKeys.CODE_VERIFIER));

        return getTokenResponse(request);
    }

    public void authorizeConsent(String consentId) {
        final String requestId = UUID.randomUUID().toString();
        final ConsentAuthorizationRequest consentAuthorization = new ConsentAuthorizationRequest();
        URL url = new URL(apiConfiguration.getBaseUrl() + Endpoints.AUTHORIZE_CONSENT);

        client.request(url.parameter(IdTags.CONSENT_ID, consentId))
                .addBearerToken(getOauthToken())
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .body(consentAuthorization, MediaType.APPLICATION_JSON_TYPE)
                .post(HttpResponse.class);
    }

    public AccountResponse fetchAccounts() {
        final String requestId = UUID.randomUUID().toString();
        URL url = new URL(apiConfiguration.getBaseUrl() + Endpoints.ACCOUNTS);
        return createRequestInSession(url)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .get(AccountResponse.class);
    }

    public TransactionResponse fetchTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final String requestId = UUID.randomUUID().toString();
        final URL fullUrl =
                new URL(
                        apiConfiguration.getBaseUrl()
                                + Endpoints.AIS_PRODUCT
                                + account.getFromTemporaryStorage(StorageKeys.TRANSACTIONS_URL));

        return createRequestInSession(fullUrl)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .queryParam(
                        QueryKeys.DATE_TO,
                        DateFormat.formatDateTime(
                                toDate, DateFormat.YEAR_MONTH_DAY, BankdataConstants.Timezone.UTC))
                .queryParam(
                        QueryKeys.DATE_FROM,
                        DateFormat.formatDateTime(
                                fromDate,
                                DateFormat.YEAR_MONTH_DAY,
                                BankdataConstants.Timezone.UTC))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(TransactionResponse.class);
    }

    public TransactionResponse fetchNextTransactions(String nextTransactionsPath) {
        final String requestId = UUID.randomUUID().toString();
        final URL fullUrl =
                new URL(
                        apiConfiguration.getBaseUrl()
                                + Endpoints.AIS_PRODUCT
                                + nextTransactionsPath);

        return createRequestInSession(fullUrl)
                .header(HeaderKeys.X_REQUEST_ID, requestId)
                .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .get(TransactionResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest paymentRequest, PaymentType type) throws PaymentException {
        final String productType = BankdataConstants.TYPE_TO_DOMAIN_MAPPER.get(type);

        URL url = new URL(apiConfiguration.getBaseUrl() + productType);

        try {
            CreatePaymentResponse response =
                    createPaymentRequestInSession(url)
                            .post(CreatePaymentResponse.class, paymentRequest);

            authorizePayment(response.getPaymentId(), type);
            return response;
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public FetchPaymentResponse fetchPayment(String paymentId, PaymentType type) {
        final String productType = BankdataConstants.TYPE_TO_DOMAIN_MAPPER.get(type);

        URL url =
                new URL(apiConfiguration.getBaseUrl() + productType + Endpoints.PAYMENT_ID)
                        .parameter(IdTags.PAYMENT_ID, paymentId);
        String paymentProduct = BankdataConstants.TYPE_TO_DOMAIN_MAPPER.get(type);

        FetchPaymentResponse fetchPaymentResponse =
                createPaymentRequestInSession(url).get(FetchPaymentResponse.class);
        PaymentStatusResponse paymentStatusResponse = getPaymentStatus(paymentProduct, paymentId);
        fetchPaymentResponse.setTransactionStatus(paymentStatusResponse.getTransactionStatus());
        return fetchPaymentResponse;
    }

    public PaymentStatusResponse getPaymentStatus(String paymentProduct, String paymentId) {
        URL url =
                new URL(apiConfiguration.getBaseUrl() + Endpoints.GET_PAYMENT_STATUS)
                        .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                        .parameter(IdTags.PAYMENT_ID, paymentId);

        return createPaymentRequestInSession(url).get(PaymentStatusResponse.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getOauthToken();

        return createRequest(url).addBearerToken(authToken);
    }

    private RequestBuilder createPaymentRequestInSession(URL url) {
        final OAuth2Token authToken = getPaymentInitialToken();
        final String requestId = UUID.randomUUID().toString();

        RequestBuilder requestBuilder =
                createRequest(url)
                        .addBearerToken(authToken)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_REQUEST_ID, requestId);

        if (apiConfiguration.isManual()) {
            requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, apiConfiguration.getUserIp());
        }
        return requestBuilder;
    }

    private AuthorizePaymentResponse authorizePayment(String paymentId, PaymentType type) {
        String productType = BankdataConstants.TYPE_TO_DOMAIN_MAPPER.get(type);
        URL url =
                new URL(apiConfiguration.getBaseUrl() + productType + Endpoints.AUTHORIZE_PAYMENT)
                        .parameter(IdTags.PAYMENT_ID, paymentId);

        return createPaymentRequestInSession(url).post(AuthorizePaymentResponse.class, "{}");
    }

    public URL getSigningPaymentUrl(String paymentId) {
        final String codeVerifier = Psd2Headers.generateCodeVerifier();
        final String codeChallenge = Psd2Headers.generateCodeChallenge(codeVerifier);
        persistentStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);

        return new URL(apiConfiguration.getBaseAuthUrl() + Endpoints.AUTHORIZE)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.SCOPE, QueryValues.PIS_SCOPE + paymentId)
                .queryParam(QueryKeys.STATE, sessionStorage.get(StorageKeys.STATE))
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUrl);
    }

    private void getTokenWithClientCredentials() {
        final InitialTokenRequest request =
                new InitialTokenRequest(FormValues.CLIENT_CREDENTIALS, FormValues.SCOPE, clientId);
        URL url = new URL(apiConfiguration.getBaseAuthUrl() + Endpoints.TOKEN);

        TokenResponse response =
                client.request(url)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(TokenResponse.class, request.toData());

        persistentStorage.put(StorageKeys.OAUTH_TOKEN, response.toTinkToken());
        persistentStorage.put(
                StorageKeys.INITIAL_TOKEN, response.toTinkToken()); // i need this token for pis
    }

    public String getConsentId() {
        getTokenWithClientCredentials();
        final ConsentRequest consentRequest = new ConsentRequest();
        final String requestId = UUID.randomUUID().toString();
        URL url = new URL(apiConfiguration.getBaseUrl() + Endpoints.CONSENT);

        ConsentResponse response =
                client.request(url)
                        .addBearerToken(getOauthToken())
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_REQUEST_ID, requestId)
                        .body(consentRequest.toData(), MediaType.APPLICATION_JSON_TYPE)
                        .post(ConsentResponse.class);

        return response.getConsentId();
    }

    OAuth2Token getOauthToken() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    private OAuth2Token getPaymentInitialToken() {
        return persistentStorage
                .get(StorageKeys.INITIAL_TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    public OAuth2Token refreshToken(String refreshToken) {
        final RefreshTokenRequest request =
                new RefreshTokenRequest(
                        FormValues.REFRESH_TOKEN_GRANT_TYPE,
                        refreshToken,
                        redirectUrl,
                        clientId,
                        persistentStorage.get(StorageKeys.CODE_VERIFIER));

        return getTokenResponse(request);
    }

    private OAuth2Token getTokenResponse(TokenRequest tokenRequest) {
        URL url = new URL(apiConfiguration.getBaseAuthUrl() + Endpoints.TOKEN);

        return client.request(url)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class, tokenRequest.toData())
                .toTinkToken();
    }

    private void handleHttpResponseException(HttpResponseException httpResponseException)
            throws PaymentException {
        if (httpResponseException.getResponse().hasBody()) {
            try {
                httpResponseException
                        .getResponse()
                        .getBody(BankdataErrorResponse.class)
                        .checkError(httpResponseException);
            } catch (HttpClientException | HttpResponseException d) {
                throw httpResponseException;
            }
        }
    }
}
