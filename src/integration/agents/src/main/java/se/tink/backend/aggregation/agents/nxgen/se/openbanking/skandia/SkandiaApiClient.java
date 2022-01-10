package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Scopes;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration.SkandiaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc.BasePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc.SignPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.payment.rpc.Payment;

public class SkandiaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private SkandiaConfiguration configuration;
    private String redirectUrl;
    private final User user;

    public SkandiaApiClient(TinkHttpClient client, PersistentStorage persistentStorage, User user) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.user = user;
    }

    private SkandiaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private URL getRedirectUrlWithState(String state) {
        return URL.of(getRedirectUrl()).queryParam(QueryKeys.STATE, state);
    }

    protected void setConfiguration(
            AgentConfiguration<SkandiaConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        client.setEidasProxy(eidasProxyConfiguration);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();
        final String clientId = getConfiguration().getClientId();
        RequestBuilder requestBuilder = createRequest(url);
        requestBuilder
                .addBearerToken(authToken)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.CLIENT_ID, clientId);
        if (user.isPresent()) {
            requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, user.getIpAddress());
        }
        return requestBuilder;
    }

    public URL getAuthorizeUrl(String state) {
        return Urls.AUTHORIZE
                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(QueryKeys.REDIRECT_URI, getRedirectUrl())
                .queryParamRaw(QueryKeys.SCOPE, getScopes())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE);
    }

    private String getScopes() {
        Set<String> scopes = configuration.getScopes();
        if (scopes.stream().allMatch(Scopes.AIS::equalsIgnoreCase)) {
            // Return only AIS scopes
            return QueryValues.SCOPE_WITHOUT_PAYMENT;
        } else if (scopes.stream()
                .allMatch(
                        scope ->
                                Scopes.AIS.equalsIgnoreCase(scope)
                                        || Scopes.PIS.equalsIgnoreCase(scope))) {
            // Return AIS + PIS scopes
            return QueryValues.SCOPE;
        } else {
            throw new IllegalArgumentException(
                    String.format(ErrorMessages.INVALID_SCOPE, scopes.toString()));
        }
    }

    public OAuth2Token getToken(String code) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        TokenRequest request =
                new TokenRequest(
                        FormValues.AUTHORIZATION_CODE,
                        code,
                        getRedirectUrl(),
                        clientId,
                        clientSecret);

        return client.request(Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        RefreshTokenRequest requestBody =
                new RefreshTokenRequest(refreshToken, clientId, clientSecret);

        try {
            return client.request(Urls.TOKEN)
                    .type(MediaType.APPLICATION_FORM_URLENCODED)
                    .post(TokenResponse.class, requestBody.toData())
                    .toTinkToken();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 400
                    && e.getResponse().getBody(ErrorResponse.class).isInvalidGrant()) {
                throw new SessionException(SessionError.SESSION_EXPIRED, e);
            }
            throw e;
        }
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(Urls.GET_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String accountId) {
        return createRequestInSession(Urls.GET_BALANCES.parameter(IdTags.ACCOUNT_ID, accountId))
                .get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactions(
            String resourceId, Date fromDate, Date toDate, String bookingStatus) {
        return createRequestInSession(
                        Urls.GET_TRANSACTIONS.parameter(IdTags.ACCOUNT_ID, resourceId))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format((toDate)))
                .queryParam(QueryKeys.BOOKING_STATUS, bookingStatus)
                .get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(Payment payment) {
        BasePaymentRequest basePaymentRequest = BasePaymentRequest.of(payment);

        return createRequestInSession(
                        SkandiaConstants.Urls.CREATE_PAYMENT.parameter(
                                SkandiaConstants.IdTags.PAYMENT_TYPE,
                                PaymentProduct.from(payment).getProduct()))
                .post(CreatePaymentResponse.class, basePaymentRequest);
    }

    public GetPaymentResponse getPayment(Payment payment) {
        return createRequestInSession(
                        SkandiaConstants.Urls.GET_PAYMENT
                                .parameter(
                                        SkandiaConstants.IdTags.PAYMENT_TYPE,
                                        PaymentProduct.from(payment).getProduct())
                                .parameter(
                                        SkandiaConstants.IdTags.PAYMENT_ID, payment.getUniqueId()))
                .get(GetPaymentResponse.class);
    }

    public SignPaymentResponse signPayment(Payment payment, String state) {

        return createRequestInSession(
                        Urls.POST_SIGN_PAYMENT
                                .parameter(
                                        SkandiaConstants.IdTags.PAYMENT_TYPE,
                                        PaymentProduct.from(payment).getProduct())
                                .parameter(
                                        SkandiaConstants.IdTags.PAYMENT_ID, payment.getUniqueId()))
                .header(HeaderKeys.TPP_REDIRECT_URI, getRedirectUrlWithState(state))
                .post(SignPaymentResponse.class, BodyValues.EMPTY_BODY);
    }
}
