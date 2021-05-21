package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthorizationResponse;
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
import se.tink.backend.aggregation.agents.utils.charsetguesser.CharsetGuesser;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@Slf4j
@RequiredArgsConstructor
public class SparkassenApiClient implements PaymentApiClient {

    private final TinkHttpClient client;
    private final SparkassenHeaderValues headerValues;
    private final SparkassenStorage storage;
    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;

    private RequestBuilder createRequest(URL url) {
        if (url.get().contains("{" + PathVariables.BANK_CODE + "}")) {
            url = url.parameter(PathVariables.BANK_CODE, headerValues.getBankCode());
        }

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID().toString())
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

    public ConsentResponse createConsent() {
        LocalDate validUntil = localDateTimeSource.now().toLocalDate().plusDays(90);
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

    public AuthorizationResponse initializeAuthorization(
            String url, String username, String password) {
        try {
            AuthorizationResponse authorizationResponse =
                    createRequest(new URL(url))
                            .header(HeaderKeys.PSU_ID, username)
                            .post(
                                    AuthorizationResponse.class,
                                    new InitAuthorizationRequest(new PsuDataEntity(password)));
            // NZG-283 temporary login
            log.info(
                    "SUCCESS_LOGIN username charset: [{}]  password charset: [{}]",
                    CharsetGuesser.getCharset(username),
                    CharsetGuesser.getCharset(password));
            return authorizationResponse;
        } catch (HttpResponseException e) {
            // NZG-283 temporary login
            log.info(
                    "FAILED_LOGIN username charset: [{}]  password charset: [{}]",
                    CharsetGuesser.getCharset(username),
                    CharsetGuesser.getCharset(password));
            SparkassenErrorHandler.handleError(
                    e, SparkassenErrorHandler.ErrorSource.AUTHORISATION_USERNAME_PASSWORD);
            throw e;
        }
    }

    public AuthorizationResponse selectAuthorizationMethod(String url, String methodId) {
        try {
            return createRequest(new URL(url))
                    .put(
                            AuthorizationResponse.class,
                            new SelectAuthenticationMethodRequest(methodId));
        } catch (HttpResponseException e) {
            SparkassenErrorHandler.handleError(
                    e, SparkassenErrorHandler.ErrorSource.AUTHORISATION_SELECT_METHOD);
            throw e;
        }
    }

    public AuthorizationResponse getAuthorizationStatus(String url) {
        return createRequest(new URL(url)).get(AuthorizationResponse.class);
    }

    public FinalizeAuthorizationResponse finalizeAuthorization(String url, String otp) {
        try {
            return createRequest(new URL(url))
                    .put(
                            FinalizeAuthorizationResponse.class,
                            new FinalizeAuthorizationRequest(otp));
        } catch (HttpResponseException e) {
            SparkassenErrorHandler.handleError(
                    e, SparkassenErrorHandler.ErrorSource.AUTHORISATION_OTP);
            throw e;
        }
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return createRequest(Urls.CONSENT_STATUS.parameter(PathVariables.CONSENT_ID, consentId))
                .get(ConsentStatusResponse.class);
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return createRequest(Urls.CONSENT_DETAILS.parameter(PathVariables.CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
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
}
