package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Endpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.PathParameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.UnicreditConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentStatusResponse;
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
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.EmptyConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class UnicreditBaseApiClient {

    private static final DateTimeFormatter CONSENT_BODY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Formats.DEFAULT_DATE_FORMAT);

    private static Logger logger = LoggerFactory.getLogger(UnicreditBaseApiClient.class);

    private final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    private String redirectUrl;
    private final Credentials credentials;
    protected UnicreditProviderConfiguration providerConfiguration;

    protected final boolean manualRequest;

    public UnicreditBaseApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            boolean manualRequest,
            UnicreditProviderConfiguration providerConfiguration) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.manualRequest = manualRequest;
        this.providerConfiguration = providerConfiguration;
    }

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

    protected URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse) {
        return new URL(consentResponse.getScaRedirect());
    }

    protected String getTransactionsDateFrom() {
        return QueryValues.TRANSACTION_FROM_DATE;
    }

    protected String getScaRedirectUrlFromCreatePaymentResponse(
            CreatePaymentResponse createPaymentResponse) {
        return createPaymentResponse.getScaRedirect();
    }

    protected Class<? extends CreatePaymentResponse> getCreatePaymentResponseType() {
        return UnicreditCreatePaymentResponse.class;
    }

    protected String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected Credentials getCredentials() {
        return Optional.ofNullable(credentials)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CREDENTIALS));
    }

    protected void setConfiguration(AgentConfiguration<EmptyConfiguration> agentConfiguration) {
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String consentId = getConsentFromStorage();

        RequestBuilder requestBuilder =
                createRequest(url)
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(HeaderKeys.CONSENT_ID, consentId);

        // This header must be present if the request was initiated by the PSU
        if (manualRequest) {
            logger.info(
                    "Request is attended -- adding PSU header for requestId = {}",
                    Psd2Headers.getRequestId());
            requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS);
        } else {
            logger.info(
                    "Request is unattended -- omitting PSU header for requestId = {}",
                    Psd2Headers.getRequestId());
        }

        return requestBuilder;
    }

    private String getConsentFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public URL buildAuthorizeUrl(String state) {

        ConsentResponse consentResponse =
                createRequest(new URL(providerConfiguration.getBaseUrl() + Endpoints.CONSENTS))
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                        .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(getRedirectUrl())
                                        .queryParam(HeaderKeys.STATE, state)
                                        .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                        .header(HeaderKeys.TPP_REDIRECT_PREFERED, true) // true for redirect auth
                        .post(getConsentResponseType(), getConsentRequest());

        persistentStorage.put(
                UnicreditConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return getScaRedirectUrlFromConsentResponse(consentResponse);
    }

    public ConsentStatusResponse getConsentStatus() throws SessionException {
        return createRequest(
                        new URL(providerConfiguration.getBaseUrl() + Endpoints.CONSENT_STATUS)
                                .parameter(PathParameters.CONSENT_ID, getConsentIdFromStorage()))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(ConsentStatusResponse.class);
    }

    public ConsentDetailsResponse getConsentDetails() throws SessionException {
        return createRequest(
                        new URL(providerConfiguration.getBaseUrl() + Endpoints.CONSENT_DETAILS)
                                .parameter(PathParameters.CONSENT_ID, getConsentIdFromStorage()))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .get(ConsentDetailsResponse.class);
    }

    public String getConsentIdFromStorage() throws SessionException {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
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

    public TransactionsResponse getTransactionsFor(TransactionalAccount account) {
        URL transactionsUrl =
                new URL(providerConfiguration.getBaseUrl() + Endpoints.TRANSACTIONS)
                        .parameter(PathParameters.ACCOUNT_ID, account.getApiIdentifier());

        RequestBuilder transactionRequestBuilder =
                createRequestInSession(transactionsUrl)
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                        .queryParam(QueryKeys.DATE_FROM, getTransactionsDateFrom())
                        .queryParam(
                                QueryKeys.DATE_TO,
                                ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()));

        return transactionRequestBuilder.get(TransactionsResponse.class);
    }

    public TransactionsResponse getTransactionsForNextUrl(URL nextUrl) {
        return createRequestInSession(nextUrl).get(TransactionsResponse.class);
    }

    public void removeConsentFromPersistentStorage() {
        persistentStorage.remove(StorageKeys.CONSENT_ID);
    }

    public CreatePaymentResponse createSepaPayment(CreatePaymentRequest request) {

        CreatePaymentResponse createPaymentResponse =
                createRequest(
                                new URL(
                                                providerConfiguration.getBaseUrl()
                                                        + Endpoints.PAYMENT_INITIATION)
                                        .parameter(
                                                PathParameters.PAYMENT_PRODUCT,
                                                UnicreditPaymentProduct.SEPA_CREDIT_TRANSFERS
                                                        .toString()))
                        .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                        .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                        .header(HeaderKeys.PSU_ID_TYPE, providerConfiguration.getPsuIdType())
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(getRedirectUrl())
                                        .queryParam(
                                                HeaderKeys.STATE,
                                                persistentStorage.get(StorageKeys.STATE))
                                        .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                        .post(getCreatePaymentResponseType(), request);

        persistentStorage.put(
                createPaymentResponse.getPaymentId(),
                getScaRedirectUrlFromCreatePaymentResponse(createPaymentResponse));

        return createPaymentResponse;
    }

    public FetchPaymentResponse fetchPayment(String paymentId) {

        return createRequest(
                        new URL(providerConfiguration.getBaseUrl() + Endpoints.FETCH_PAYMENT)
                                .parameter(
                                        PathParameters.PAYMENT_PRODUCT,
                                        UnicreditPaymentProduct.SEPA_CREDIT_TRANSFERS.toString())
                                .parameter(PathParameters.PAYMENT_ID, paymentId))
                .header(HeaderKeys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(FetchPaymentResponse.class);
    }
}
