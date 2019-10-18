package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.BodyKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.BodyValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Psu;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.UrlParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.BalanceAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.rpc.HandelsbankenErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class HandelsbankenBaseApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private HandelsbankenBaseConfiguration configuration;

    public HandelsbankenBaseApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public void setConfiguration(HandelsbankenBaseConfiguration configuration) {
        this.configuration = configuration;
    }

    public HandelsbankenBaseConfiguration getConfiguration() {
        return this.configuration;
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

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.X_IBM_CLIENT_ID, this.configuration.getClientId())
                .addBearerToken(getOauthToken())
                .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, Psu.IP_ADDRESS)
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

    public DecoupledResponse getDecoupled(URL href) {
        try {
            return client.request(href)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .type(MediaType.APPLICATION_JSON)
                    .post(DecoupledResponse.class);
        } catch (HttpResponseException e) {
            if (HttpStatus.SC_BAD_REQUEST == e.getResponse().getStatus()) {
                return e.getResponse().getBody(DecoupledResponse.class);
            }
            throw e;
        }
    }

    public TokenResponse refreshAccessToken(String refreshToken) {

        final Form params =
                Form.builder()
                        .put(BodyKeys.GRANT_TYPE, BodyValues.REFRESH_TOKEN)
                        .put(BodyKeys.REFRESH_TOKEN, refreshToken)
                        .put(BodyKeys.CLIENT_ID, configuration.getClientId())
                        .build();

        return client.request(Urls.TOKEN)
                .body(params.toString())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, HandelsbankenPaymentType paymentProduct)
            throws PaymentException {
        try {
            return createRequest(
                            new URL(Urls.INITIATE_PAYMENT)
                                    .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct.toString()))
                    .post(CreatePaymentResponse.class, createPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public ConfirmPaymentResponse confirmPayment(
            String paymentId, HandelsbankenPaymentType paymentProduct) throws PaymentException {
        try {
            return createRequest(
                            new URL(Urls.CONFIRM_PAYMENT)
                                    .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct.toString())
                                    .parameter(IdTags.PAYMENT_ID, paymentId))
                    .put(ConfirmPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public GetPaymentResponse getPayment(String paymentId, String paymentProduct)
            throws PaymentException {
        try {
            return createRequest(
                            new URL(Urls.GET_PAYMENT)
                                    .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                    .parameter(IdTags.PAYMENT_ID, paymentId))
                    .get(GetPaymentResponse.class);
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

    public TokenResponse requestClientCredentialGrantToken() {
        final Form params =
                Form.builder()
                        .put(BodyKeys.GRANT_TYPE, BodyValues.CLIENT_CREDENTIALS)
                        .put(BodyKeys.SCOPE, BodyValues.AIS_SCOPE)
                        .put(BodyKeys.CLIENT_ID, configuration.getClientId())
                        .put(BodyKeys.PSU_ID_TYPE, "1")
                        .build();

        return client.request(Urls.TOKEN)
                .body(params.toString())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class);
    }

    public AuthorizationResponse initiateConsent(String accessToken) {
        return client.request(Urls.AUTHORIZATION)
                .body(new AuthorizationRequest(HandelsbankenBaseConstants.BodyValues.ALL_ACCOUNTS))
                .header(HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.COUNTRY, HandelsbankenBaseConstants.Market.COUNTRY)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        HandelsbankenBaseConstants.HeaderKeys.BEARER + accessToken)
                .header(HeaderKeys.PSU_IP_ADDRESS, Psu.IP_ADDRESS)
                .header(HeaderKeys.TPP_TRANSACTION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.TPP_REQUEST_ID, UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(AuthorizationResponse.class);
    }

    public SessionResponse initDecoupledAuthorization(String ssn, String consentId) {
        return client.request(Urls.SESSION)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .body(
                        new SessionRequest(
                                configuration.getClientId(),
                                BodyValues.AIS_SCOPE + ":" + consentId,
                                Psu.IP_ADDRESS,
                                ssn,
                                BodyValues.PERSONAL_ID_TP))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_JSON)
                .post(SessionResponse.class);
    }

    /**
     * The SHB access token lives for 24 hours on the millisecond. In some cases the access token is
     * still active during the authentication, but expires directly after later. This method will
     * refresh the access token if it's expired, and will then retry the request with the new token.
     */
    private <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            return request.get(responseType);

        } catch (HttpResponseException hre) {
            verifyIsTokenNotActiveErrorOrThrow(hre);

            refreshAndStoreOauthToken();

            request.overrideHeader(HttpHeaders.AUTHORIZATION, getOauthToken().toAuthorizeHeader());
        }

        // retry request with new token
        return request.get(responseType);
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

        TokenResponse response = refreshAccessToken(refreshToken);

        OAuth2Token oAuth2Token =
                OAuth2Token.create(
                        QueryKeys.BEARER,
                        response.getAccessToken(),
                        refreshToken,
                        response.getExpiresIn());

        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
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
}
