package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.BodyKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Errors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.OAuth2Type;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Psu;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Scope;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.RedirectResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.rpc.CreditAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.BalanceAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.rpc.HandelsbankenErrorResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class HandelsbankenBaseApiClient {

    private static final Logger logger = LoggerFactory.getLogger(HandelsbankenBaseApiClient.class);

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private HandelsbankenBaseConfiguration configuration;
    private String redirectUrl;
    private final String market;

    public HandelsbankenBaseApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, String market) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.market = market;
    }

    public void setConfiguration(
            AgentConfiguration<HandelsbankenBaseConfiguration> agentConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    public HandelsbankenBaseConfiguration getConfiguration() {
        return this.configuration;
    }

    public String getRedirectUrl() {
        return this.redirectUrl;
    }

    private OAuth2Token getOauthToken() {
        return persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        HandelsbankenBaseConstants.ExceptionMessages
                                                .TOKEN_NOT_FOUND));
    }

    private OAuth2Token getPisOauthToken() {
        return persistentStorage
                .get(StorageKeys.PIS_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        HandelsbankenBaseConstants.ExceptionMessages
                                                .TOKEN_NOT_FOUND));
    }

    private RequestBuilder createRequest(URL url) {

        return client.request(url)
                .addBearerToken(getOauthToken())
                .header(HeaderKeys.X_IBM_CLIENT_ID, this.configuration.getClientId())
                .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, Psu.IP_ADDRESS)
                .header(HeaderKeys.COUNTRY, market)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createPisRequest(URL url) {

        return client.request(url)
                .addBearerToken(getPisOauthToken())
                .header(HeaderKeys.X_IBM_CLIENT_ID, this.configuration.getClientId())
                .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, Psu.IP_ADDRESS)
                .header(HeaderKeys.COUNTRY, market)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON);
    }

    public AccountsResponse getAccountList() {
        return requestRefreshableGet(createRequest(Urls.ACCOUNTS), AccountsResponse.class);
    }

    public BalanceAccountResponse getAccountDetails(String accountId) {
        RequestBuilder request =
                createRequest(Urls.ACCOUNT_DETAILS.parameter(UrlParams.ACCOUNT_ID, accountId))
                        .queryParam(QueryKeys.WITH_BALANCE, Boolean.TRUE.toString());

        return requestRefreshableGet(request, BalanceAccountResponse.class);
    }

    public TransactionResponse getTransactions(String accountId, Date dateFrom, Date dateTo) {
        RequestBuilder request =
                createRequest(Urls.ACCOUNT_TRANSACTIONS.parameter(UrlParams.ACCOUNT_ID, accountId))
                        .queryParam(
                                QueryKeys.DATE_FROM,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(dateFrom))
                        .queryParam(
                                QueryKeys.DATE_TO,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(dateTo));

        return requestRefreshableGet(request, TransactionResponse.class);
    }

    public CreditAccountResponse getCreditAccounts() {
        try {
            return requestRefreshableGet(
                    createRequest(Urls.CARD_ACCOUNTS), CreditAccountResponse.class);
        } catch (HttpResponseException e) {
            HandelsbankenErrorResponse errorResponse =
                    e.getResponse().getBody(HandelsbankenErrorResponse.class);
            if (errorResponse.hasNotRegisteredToPlanError()) {
                logger.info("App is not registered to credit cards API.");
                return new CreditAccountResponse();
            }
            throw e;
        }
    }

    public TransactionResponse getCreditTransactions(String accountId, Date dateFrom, Date dateTo) {
        RequestBuilder request =
                createRequest(Urls.CARD_TRANSACTIONS.parameter(UrlParams.ACCOUNT_ID, accountId))
                        .queryParam(
                                QueryKeys.DATE_FROM,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(dateFrom))
                        .queryParam(
                                QueryKeys.DATE_TO,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(dateTo));

        return requestRefreshableGet(request, TransactionResponse.class);
    }

    public DecoupledResponse getDecoupled(URL href) {
        try {
            RequestBuilder requestBuilder =
                    client.request(href)
                            .accept(MediaType.APPLICATION_JSON_TYPE)
                            .type(MediaType.APPLICATION_JSON);
            return post(requestBuilder, DecoupledResponse.class);
        } catch (HttpResponseException e) {
            if (HttpStatus.SC_BAD_REQUEST == e.getResponse().getStatus()) {
                return e.getResponse().getBody(DecoupledResponse.class);
            }
            throw e;
        }
    }

    public SessionResponse initDecoupledAuthorizationAis(String personalId, String consentId) {

        RequestBuilder requestBuilder =
                client.request(Urls.SESSION)
                        .body(
                                new SessionRequest(
                                        getConfiguration().getClientId(),
                                        Scope.AIS + ":" + consentId,
                                        Psu.IP_ADDRESS,
                                        personalId,
                                        BodyValues.PERSONAL_ID_TP))
                        .header(HeaderKeys.CONSENT_ID, consentId)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .type(MediaType.APPLICATION_JSON);
        return post(requestBuilder, SessionResponse.class);
    }

    public SessionResponse initDecoupledAuthorizationPis(String personalId, String paymentId) {

        RequestBuilder requestBuilder =
                client.request(Urls.SESSION)
                        .body(
                                new SessionRequest(
                                        getConfiguration().getClientId(),
                                        Scope.PIS + ":" + paymentId,
                                        Psu.IP_ADDRESS,
                                        personalId,
                                        BodyValues.PERSONAL_ID_TP))
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .type(MediaType.APPLICATION_JSON);
        return post(requestBuilder, SessionResponse.class);
    }

    public TokenResponse getRefreshToken(String refreshToken) {

        final Form params =
                Form.builder()
                        .put(BodyKeys.GRANT_TYPE, BodyValues.REFRESH_TOKEN)
                        .put(BodyKeys.REFRESH_TOKEN, refreshToken)
                        .put(BodyKeys.CLIENT_ID, configuration.getClientId())
                        .build();

        RequestBuilder requestBuilder =
                client.request(Urls.TOKEN)
                        .body(params.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        return post(requestBuilder, TokenResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, HandelsbankenPaymentType paymentProduct)
            throws PaymentException {
        try {
            TokenResponse response = requestClientCredentialGrantTokenWithScope(Scope.PIS);

            OAuth2Token clientCredentialPaymentToken =
                    OAuth2Token.create(
                            OAuth2Type.BEARER,
                            response.getAccessToken(),
                            null,
                            response.getExpiresIn());

            persistentStorage.put(StorageKeys.PIS_TOKEN, clientCredentialPaymentToken);
            URL url =
                    new URL(Urls.INITIATE_PAYMENT)
                            .parameter(QueryKeys.PAYMENT_PRODUCT, paymentProduct.toString());
            RequestBuilder requestBuilder =
                    client.request(url)
                            .addBearerToken(clientCredentialPaymentToken)
                            .header(HeaderKeys.X_IBM_CLIENT_ID, this.configuration.getClientId())
                            .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                            .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                            .header(HeaderKeys.PSU_IP_ADDRESS, Psu.IP_ADDRESS)
                            .header(HeaderKeys.COUNTRY, market)
                            .accept(MediaType.APPLICATION_JSON_TYPE)
                            .type(MediaType.APPLICATION_JSON);
            return post(requestBuilder, CreatePaymentResponse.class, createPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public ConfirmPaymentResponse confirmPayment(
            String paymentId, HandelsbankenPaymentType paymentProduct) throws PaymentException {
        try {
            RequestBuilder requestBuilder =
                    createPisRequest(
                            new URL(Urls.CONFIRM_PAYMENT)
                                    .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct.toString())
                                    .parameter(IdTags.PAYMENT_ID, paymentId));
            return put(requestBuilder, ConfirmPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public GetPaymentResponse fetchPayment(String paymentId, String paymentProduct)
            throws PaymentException {
        try {
            RequestBuilder requestBuilder =
                    createPisRequest(
                            new URL(Urls.GET_PAYMENT)
                                    .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                    .parameter(IdTags.PAYMENT_ID, paymentId));
            return get(requestBuilder, GetPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    private void handleHttpResponseException(HttpResponseException httpResponseException)
            throws PaymentException {
        if (httpResponseException.getResponse().hasBody()) {
            try {
                httpResponseException
                        .getResponse()
                        .getBody(HandelsbankenErrorResponse.class)
                        .parseAndThrow(httpResponseException);
            } catch (HttpClientException | HttpResponseException d) {
                throw httpResponseException;
            }
        }
    }

    public TokenResponse requestClientCredentialGrantTokenWithScope(String scope) {

        final Form params =
                Form.builder()
                        .put(BodyKeys.GRANT_TYPE, BodyValues.CLIENT_CREDENTIALS)
                        .put(BodyKeys.SCOPE, scope)
                        .put(BodyKeys.CLIENT_ID, getConfiguration().getClientId())
                        .put(BodyKeys.PSU_ID_TYPE, "1")
                        .build();

        RequestBuilder requestBuilder =
                client.request(Urls.TOKEN)
                        .body(params.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        return post(requestBuilder, TokenResponse.class);
    }

    public AuthorizationResponse initiateConsent(String code) {

        RequestBuilder requestBuilder =
                client.request(Urls.AUTHORIZATION)
                        .body(
                                new AuthorizationRequest(
                                        HandelsbankenBaseConstants.BodyValues.ALL_ACCOUNTS))
                        .header(HeaderKeys.X_IBM_CLIENT_ID, getConfiguration().getClientId())
                        .header(HeaderKeys.COUNTRY, market)
                        .header(
                                HeaderKeys.AUTHORIZATION,
                                HandelsbankenBaseConstants.HeaderKeys.BEARER + code)
                        .header(HeaderKeys.PSU_IP_ADDRESS, Psu.IP_ADDRESS)
                        .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                        .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .type(MediaType.APPLICATION_JSON);
        return post(requestBuilder, AuthorizationResponse.class);
    }

    /**
     * The SHB access token lives for 24 hours on the millisecond. In some cases the access token is
     * still active during the authentication, but expires directly after later. This method will
     * refresh the access token if it's expired, and will then retry the request with the new token.
     */
    private <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            return get(request, responseType);
        } catch (HttpResponseException hre) {
            verifyKnowYourCustomer(hre);
            verifyIsTokenNotActiveErrorOrThrow(hre);
            refreshAndStoreOauthToken();
            request.overrideHeader(HttpHeaders.AUTHORIZATION, getOauthToken().toAuthorizeHeader());
        }
        // retry request with new token
        return get(request, responseType);
    }

    private <T> T get(RequestBuilder requestBuilder, Class<T> responseType) {
        try {
            return requestBuilder.get(responseType);
        } catch (HttpResponseException e) {
            handleException(e);
            throw e;
        }
    }

    private <T> T post(RequestBuilder requestBuilder, Class<T> responseType) {
        return post(requestBuilder, responseType, null);
    }

    private <T, A> T post(RequestBuilder requestBuilder, Class<T> responseType, A requestBody) {
        try {
            if (Objects.isNull(requestBody)) {
                return requestBuilder.post(responseType);
            } else {
                return requestBuilder.post(responseType, requestBody);
            }
        } catch (HttpResponseException e) {
            handleException(e);
            throw e;
        }
    }

    private <T> T put(RequestBuilder requestBuilder, Class<T> responseType) {
        try {
            return requestBuilder.put(responseType);
        } catch (HttpResponseException e) {
            handleException(e);
            throw e;
        }
    }

    private void handleException(HttpResponseException e) {
        if (e.getResponse().getStatus() == HttpStatus.SC_BAD_GATEWAY) {
            if (isBankServiceError(e.getResponse())) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
            }
        }
    }

    private boolean isBankServiceError(HttpResponse response) {
        String body = response.getBody(String.class);
        return Pattern.compile(Pattern.quote(Errors.PROXY_ERROR), Pattern.CASE_INSENSITIVE)
                        .matcher(body)
                        .find()
                && Pattern.compile(Pattern.quote(Errors.SOCKET_EXCEPTION), Pattern.CASE_INSENSITIVE)
                        .matcher(body)
                        .find();
    }

    /**
     * Refresh access token and update stored OAuth token. Usually handled by the authentication
     * controller, but in some edge cases the access token expires between login and refresh. Then
     * it needs to be refreshed by the agent directly.
     */
    private void refreshAndStoreOauthToken() {
        String refreshToken =
                getOauthToken()
                        .getRefreshToken()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find refresh token to refresh."));

        TokenResponse response = getRefreshToken(refreshToken);

        OAuth2Token oAuth2Token =
                OAuth2Token.create(
                        QueryKeys.BEARER,
                        response.getAccessToken(),
                        refreshToken,
                        response.getExpiresIn());

        persistentStorage.rotateStorageValue(PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
    }

    private void verifyIsTokenNotActiveErrorOrThrow(HttpResponseException hre) {
        HttpResponse response = hre.getResponse();

        if (!(response.getStatus() == HttpStatus.SC_UNAUTHORIZED)) {
            // Unexpected exception, throw it.
            throw hre;
        }

        HandelsbankenErrorResponse errorResponse =
                response.getBody(HandelsbankenErrorResponse.class);

        if (!errorResponse.isTokenNotActiveError()) {
            // Unexpected error message, throw original exception.
            throw hre;
        }
    }

    private void verifyKnowYourCustomer(HttpResponseException hre) {
        if (hre.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                    HandelsbankenBaseConstants.UnacceptedTermsAndConditionsException
                            .KNOW_YOUR_CUSTOMER);
        }
    }

    public RedirectResponse exchangeAuthorizationCode(String consentId, String code) {
        Form tokenForm =
                Form.builder()
                        .put(BodyKeys.GRANT_TYPE, "authorization_code")
                        .put(BodyKeys.SCOPE, "AIS:" + consentId)
                        .put(BodyKeys.CLIENT_ID, configuration.getClientId())
                        .put("code", code)
                        .put("redirect_uri", getRedirectUrl())
                        .build();

        return client.request(Urls.TOKEN)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .body(tokenForm.serialize())
                .post(RedirectResponse.class);
    }
}
