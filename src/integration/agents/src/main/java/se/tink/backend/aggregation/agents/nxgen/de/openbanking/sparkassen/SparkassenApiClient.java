package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.BLOCKED_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.CHALLENGE_FORMAT_INVALID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.FORMAT_ERROR;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.NO_ACTIVE_TAN_MEDIUM;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.OTP_FORMAT_ERROR;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.PLEASE_CHANGE_PIN;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.PSU_CREDENTIALS_INVALID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.PSU_ID_TOO_LONG;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.ErrorMessages.TEMPORARILY_BLOCKED_ACCOUNT;

import java.time.LocalDate;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.OauthEndpointsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class SparkassenApiClient implements PaymentApiClient {

    private final TinkHttpClient client;
    private final SparkassenHeaderValues headerValues;
    private final SparkassenStorage storage;
    private final Provider provider;

    private RequestBuilder createRequest(URL url) {
        if (url.get().contains("{" + PathVariables.BANK_CODE + "}")) {
            url = url.parameter(PathVariables.BANK_CODE, headerValues.getBankCode());
        }

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.PSU_IP_ADDRESS, headerValues.getUserIp());
    }

    private RequestBuilder createRequestInSession(URL url, String consentId) {
        String auth = storage.getToken().map(OAuth2Token::toAuthorizeHeader).orElse(null);
        return createRequest(url)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header("Authorization", auth);
    }

    public OauthEndpointsResponse getOauthEndpoints(String authorizationEndpointSource) {
        return client.request(authorizationEndpointSource)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(OauthEndpointsResponse.class);
    }

    public ConsentResponse createConsent() throws LoginException {
        LocalDate validUntil = LocalDate.now().plusDays(90);
        ConsentRequest consentRequest =
                new ConsentRequest(
                        new AccessEntity(),
                        true,
                        validUntil.toString(),
                        FormValues.FREQUENCY_PER_DAY,
                        false);

        return createRequest(Urls.CONSENT)
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, headerValues.isRedirect())
                .header("TPP-Redirect-URI", headerValues.getRedirectUrl())
                .header("TPP-Nok-Redirect-URI", headerValues.getRedirectUrl())
                .post(ConsentResponse.class, consentRequest);
    }

    public AuthenticationMethodResponse initializeAuthorization(
            URL url, String username, String password) throws AuthenticationException {
        try {
            return createRequest(url)
                    .header(HeaderKeys.PSU_ID, username)
                    .post(
                            AuthenticationMethodResponse.class,
                            new InitAuthorizationRequest(new PsuDataEntity(password)));
        } catch (HttpResponseException e) {
            // ITE-2489 - temporary experiment
            SparkassenExperimentalLoginErrorHandling.handleIncorrectLogin(e, provider);
            String errorBody = e.getResponse().getBody(String.class);

            if (errorBody.contains(PSU_CREDENTIALS_INVALID)) {
                if (errorBody.contains(TEMPORARILY_BLOCKED_ACCOUNT)
                        || errorBody.contains(BLOCKED_ACCOUNT)) {
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception(e);
                } else {
                    throw LoginError.INCORRECT_CREDENTIALS.exception(e);
                }
            }

            if (errorBody.contains(FORMAT_ERROR) && errorBody.contains(PSU_ID_TOO_LONG)) {
                throw LoginError.INCORRECT_CREDENTIALS.exception(e);
            }

            if (errorBody.contains(NO_ACTIVE_TAN_MEDIUM)) {
                throw LoginError.NO_AVAILABLE_SCA_METHODS.exception(e);
            }
            if (errorBody.contains(PLEASE_CHANGE_PIN)) {
                throw LoginError.PASSWORD_CHANGE_REQUIRED.exception(e);
            }

            throw e;
        }
    }

    public AuthenticationMethodResponse selectAuthorizationMethod(
            String consentId, String authorizationId, String methodId) {
        return createRequest(
                        Urls.UPDATE_SCA_METHOD
                                .parameter(PathVariables.CONSENT_ID, consentId)
                                .parameter(PathVariables.AUTHORIZATION_ID, authorizationId))
                .put(
                        AuthenticationMethodResponse.class,
                        new SelectAuthenticationMethodRequest(methodId));
    }

    public FinalizeAuthorizationResponse finalizeAuthorization(
            String consentId, String authorizationId, String otp) {
        try {
            return createRequest(
                            Urls.FINALIZE_AUTHORIZATION
                                    .parameter(PathVariables.CONSENT_ID, consentId)
                                    .parameter(PathVariables.AUTHORIZATION_ID, authorizationId))
                    .put(
                            FinalizeAuthorizationResponse.class,
                            new FinalizeAuthorizationRequest(otp));
        } catch (HttpResponseException e) {
            String errorBody = e.getResponse().getBody(String.class);
            if (errorBody.contains(PSU_CREDENTIALS_INVALID)
                    || errorBody.contains(CHALLENGE_FORMAT_INVALID)
                    || (errorBody.contains(FORMAT_ERROR) && errorBody.contains(OTP_FORMAT_ERROR))) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
            }
            throw e;
        }
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return createRequest(Urls.CONSENT_STATUS.parameter(PathVariables.CONSENT_ID, consentId))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(ConsentStatusResponse.class);
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        try {
            return createRequest(
                            Urls.CONSENT_DETAILS.parameter(PathVariables.CONSENT_ID, consentId))
                    .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                    .get(ConsentDetailsResponse.class);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 503) {
                throw BankServiceError.NO_BANK_SERVICE.exception(e);
            } else {
                throw e;
            }
        }
    }

    public FetchAccountsResponse fetchAccounts(String consentId) {
        return createRequestInSession(SparkassenConstants.Urls.FETCH_ACCOUNTS, consentId)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse getAccountBalance(String consentId, String accountId) {
        return createRequestInSession(
                        Urls.FETCH_BALANCES.parameter(PathVariables.ACCOUNT_ID, accountId),
                        consentId)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .get(FetchBalancesResponse.class);
    }

    public String fetchTransactions(String consentId, String accountId, LocalDate startDate) {
        return createRequestInSession(
                        Urls.FETCH_TRANSACTIONS
                                .parameter(PathVariables.ACCOUNT_ID, accountId)
                                .queryParam(QueryKeys.DATE_FROM, startDate.toString())
                                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH),
                        consentId)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_XML)
                .get(String.class);
    }

    public TokenResponse sendToken(String tokenEndpoint, String tokenEntity) {
        return createRequest(new URL(tokenEndpoint))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenEntity);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest request, PaymentRequest paymentRequest) {
        return createRequest(
                        SparkassenConstants.Urls.PAYMENT_INITIATION
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_SERVICE,
                                        PaymentService.getPaymentService(
                                                paymentRequest
                                                        .getPayment()
                                                        .getPaymentServiceType()))
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_PRODUCT,
                                        PaymentProduct.getPaymentProduct(
                                                paymentRequest.getPayment().getPaymentScheme())))
                .header(PaymentConstants.HeaderKeys.TPP_REJECTION_NOFUNDS_PREFERRED, true)
                .header(SparkassenConstants.HeaderKeys.TPP_REDIRECT_PREFERRED, false)
                .post(CreatePaymentResponse.class, request);
    }

    public FetchPaymentStatusResponse fetchPaymentStatus(PaymentRequest paymentRequest) {

        String paymentId = paymentRequest.getPayment().getUniqueId();
        return createRequest(
                        SparkassenConstants.Urls.FETCH_PAYMENT_STATUS
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_SERVICE,
                                        PaymentService.getPaymentService(
                                                paymentRequest
                                                        .getPayment()
                                                        .getPaymentServiceType()))
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_PRODUCT,
                                        PaymentProduct.getPaymentProduct(
                                                paymentRequest.getPayment().getPaymentScheme()))
                                .parameter(PaymentConstants.PathVariables.PAYMENT_ID, paymentId))
                .get(FetchPaymentStatusResponse.class);
    }

    public FinalizeAuthorizationResponse finalizePaymentAuthorization(
            String transactionAuthorizationUrl, String otp) {
        try {
            return createRequest(new URL(transactionAuthorizationUrl))
                    .put(
                            FinalizeAuthorizationResponse.class,
                            new FinalizeAuthorizationRequest(otp));
        } catch (HttpResponseException e) {
            String errorBody = e.getResponse().getBody(String.class);
            if (errorBody.contains(PSU_CREDENTIALS_INVALID)
                    || errorBody.contains(CHALLENGE_FORMAT_INVALID)
                    || (errorBody.contains(FORMAT_ERROR) && errorBody.contains(OTP_FORMAT_ERROR))) {
                throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(e);
            }
            throw e;
        }
    }

    public AuthenticationMethodResponse selectPaymentAuthorizationMethod(
            String url, String methodId) {
        return createRequest(new URL(url))
                .put(
                        AuthenticationMethodResponse.class,
                        new SelectAuthenticationMethodRequest(methodId));
    }
}
