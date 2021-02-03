package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.PathParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.UnicreditConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.enums.UnicreditPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc.UnicreditCreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.date.ThreadSafeDateFormat;

@Slf4j
@RequiredArgsConstructor
public class UnicreditBaseApiClient {

    private static final DateTimeFormatter CONSENT_BODY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Formats.DEFAULT_DATE_FORMAT);

    private final TinkHttpClient client;
    protected final UnicreditStorage unicreditStorage;
    protected final UnicreditProviderConfiguration providerConfiguration;
    protected final UnicreditBaseHeaderValues headerValues;

    protected ConsentRequest getConsentRequest() {
        LocalDateTime validUntil =
                LocalDateTime.now().plusDays(FormValues.CONSENT_VALIDATION_PERIOD_IN_DAYS);

        return new ConsentRequest(
                new UnicreditConsentAccessEntity(FormValues.ALL_ACCOUNTS),
                true,
                CONSENT_BODY_DATE_FORMATTER.format(validUntil),
                FormValues.FREQUENCY_PER_DAY,
                false);
    }

    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return UnicreditConsentResponse.class;
    }

    public URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse) {
        return new URL(consentResponse.getScaRedirect());
    }

    protected String getScaRedirectUrlFromCreatePaymentResponse(
            CreatePaymentResponse createPaymentResponse) {
        return createPaymentResponse.getScaRedirect();
    }

    protected Class<? extends CreatePaymentResponse> getCreatePaymentResponseType() {
        return UnicreditCreatePaymentResponse.class;
    }

    protected RequestBuilder createRequest(URL url) {
        return createRequestBuilder(url)
                .header(HeaderKeys.PSU_IP_ADDRESS, headerValues.getUserIp());
    }

    protected RequestBuilder createRequestBuilder(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String consentId = getConsentIdFromStorage();
        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.CONSENT_ID, consentId);
    }

    public ConsentResponse createConsent(String state) {
        return createRequest(new URL(providerConfiguration.getBaseUrl() + Endpoints.CONSENTS))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(headerValues.getRedirectUrl())
                                .queryParam(HeaderKeys.STATE, state)
                                .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                .header(HeaderKeys.TPP_REDIRECT_PREFERED, true) // true for redirect auth
                .post(getConsentResponseType(), getConsentRequest());
    }

    public ConsentDetailsResponse getConsentDetails() {
        return createRequest(
                        new URL(providerConfiguration.getBaseUrl() + Endpoints.CONSENT_DETAILS)
                                .parameter(PathParameters.CONSENT_ID, getConsentIdFromStorage()))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(ConsentDetailsResponse.class);
    }

    private String getConsentIdFromStorage() {
        return unicreditStorage
                .getConsentId()
                .orElseThrow(() -> new IllegalStateException("Missing consent id"));
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(
                        new URL(providerConfiguration.getBaseUrl() + Endpoints.ACCOUNTS))
                .get(AccountsResponse.class);
    }

    public BalancesResponse fetchAccountBalance(String accountId) {
        return createRequestInSession(
                        new URL(providerConfiguration.getBaseUrl() + Endpoints.BALANCES)
                                .parameter(PathParameters.ACCOUNT_ID, accountId))
                .get(BalancesResponse.class);
    }

    public AccountDetailsResponse fetchAccountDetails(String accountId) {
        return createRequestInSession(
                        new URL(providerConfiguration.getBaseUrl() + Endpoints.ACCOUNT_DETAILS)
                                .parameter(PathParameters.ACCOUNT_ID, accountId))
                .get(AccountDetailsResponse.class);
    }

    public TransactionsResponse getTransactionsFor(TransactionalAccount account, Date dateFrom) {
        URL transactionsUrl =
                new URL(providerConfiguration.getBaseUrl() + Endpoints.TRANSACTIONS)
                        .parameter(PathParameters.ACCOUNT_ID, account.getApiIdentifier());

        RequestBuilder transactionRequestBuilder =
                createRequestInSession(transactionsUrl)
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                        .queryParam(
                                QueryKeys.DATE_FROM,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(dateFrom))
                        .queryParam(
                                QueryKeys.DATE_TO,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()));

        return transactionRequestBuilder.get(TransactionsResponse.class);
    }

    public TransactionsResponse getTransactionsForNextUrl(URL nextUrl) {
        return createRequestInSession(nextUrl).get(TransactionsResponse.class);
    }

    public CreatePaymentResponse createSepaPayment(
            CreatePaymentRequest request, PaymentRequest paymentRequest) {
        String psuIpAddress =
                Optional.ofNullable(paymentRequest.getOriginatingUserIp())
                        .orElse(HeaderValues.PSU_IP_ADDRESS);

        String paymentProduct =
                UnicreditConstants.PAYMENT_PRODUCT_MAPPER
                        .translate(paymentRequest.getPayment().getPaymentScheme())
                        .orElse(UnicreditPaymentProduct.SEPA_CREDIT_TRANSFERS.toString());
        CreatePaymentResponse createPaymentResponse =
                createRequestBuilder(
                                new URL(
                                                providerConfiguration.getBaseUrl()
                                                        + Endpoints.PAYMENT_INITIATION)
                                        .parameter(PathParameters.PAYMENT_PRODUCT, paymentProduct))
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress)
                        .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(headerValues.getRedirectUrl())
                                        .queryParam(
                                                HeaderKeys.STATE,
                                                unicreditStorage
                                                        .getAuthenticationState()
                                                        .orElse(null))
                                        .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                        .post(getCreatePaymentResponseType(), request);

        unicreditStorage.saveScaRedirectUrlForPayment(
                createPaymentResponse.getPaymentId(),
                getScaRedirectUrlFromCreatePaymentResponse(createPaymentResponse));

        return createPaymentResponse;
    }

    public FetchPaymentResponse fetchPayment(PaymentRequest paymentRequest) {

        String psuIpAddress =
                Optional.ofNullable(paymentRequest.getOriginatingUserIp())
                        .orElse(HeaderValues.PSU_IP_ADDRESS);
        String paymentProduct =
                UnicreditConstants.PAYMENT_PRODUCT_MAPPER
                        .translate(paymentRequest.getPayment().getPaymentScheme())
                        .orElse(UnicreditPaymentProduct.SEPA_CREDIT_TRANSFERS.toString());
        String paymentId = paymentRequest.getPayment().getUniqueId();
        return createRequestBuilder(
                        new URL(providerConfiguration.getBaseUrl() + Endpoints.FETCH_PAYMENT)
                                .parameter(PathParameters.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(PathParameters.PAYMENT_ID, paymentId))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress)
                .get(FetchPaymentResponse.class);
    }
}
