package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.PathParameterValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentPsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@Slf4j
public class CbiGlobeApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private CbiGlobeConfiguration configuration;
    protected String redirectUrl;
    protected TemporaryStorage temporaryStorage;
    protected InstrumentType instrumentType;
    private CbiGlobeProviderConfiguration providerConfiguration;
    protected final String psuIpAddress;

    public CbiGlobeApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TemporaryStorage temporaryStorage,
            InstrumentType instrumentType,
            CbiGlobeProviderConfiguration providerConfiguration,
            String psuIpAddress) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.temporaryStorage = temporaryStorage;
        this.instrumentType = instrumentType;
        this.providerConfiguration = providerConfiguration;
        this.psuIpAddress = psuIpAddress;
    }

    protected CbiGlobeConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(AgentConfiguration<CbiGlobeConfiguration> agentConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url).type(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url)
                .addBearerToken(authToken)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.ASPSP_CODE, providerConfiguration.getAspspCode())
                .header(HeaderKeys.DATE, CbiGlobeUtils.formatDate(new Date()));
    }

    protected RequestBuilder createRequestWithConsent(URL url) {
        return createRequestInSession(url)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    protected RequestBuilder createAccountsRequestWithConsent() {
        RequestBuilder rb =
                createRequestInSession(getAccountsUrl())
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID));
        return addPsuIpAddressHeaderIfNeeded(rb);
    }

    private URL getAccountsUrl() {
        return this.instrumentType.equals(InstrumentType.ACCOUNTS)
                ? Urls.ACCOUNTS
                : Urls.CARD_ACCOUNTS;
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        MessageCodes.NO_ACCESS_TOKEN_IN_STORAGE.name()));
    }

    public GetTokenResponse getToken(String clientId, String clientSecret) {
        return createRequest(Urls.TOKEN)
                .addBasicAuth(clientId, clientSecret)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.CLIENT_CREDENTIALS)
                .queryParam(QueryKeys.SCOPE, QueryValues.PRODUCTION)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class);
    }

    public void getAndSaveToken() {
        GetTokenResponse getTokenResponse =
                getToken(configuration.getClientId(), configuration.getClientSecret());
        OAuth2Token token = getTokenResponse.toTinkToken();
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
    }

    public ConsentResponse createConsent(
            String state, ConsentType consentType, ConsentRequest consentRequest) {
        RequestBuilder request = createConsentRequest(state, consentType);
        return request.post(ConsentResponse.class, consentRequest);
    }

    protected RequestBuilder createConsentRequest(String state, ConsentType consentType) {
        String okFullRedirectUrl = createRedirectUrl(state, consentType, QueryValues.SUCCESS);
        String nokFullRedirectUrl = createRedirectUrl(state, consentType, QueryValues.FAILURE);
        return createRequestInSession(Urls.CONSENTS)
                .header(HeaderKeys.ASPSP_PRODUCT_CODE, providerConfiguration.getAspspProductCode())
                .header(HeaderKeys.TPP_REDIRECT_URI, okFullRedirectUrl)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, nokFullRedirectUrl);
    }

    public String createRedirectUrl(String state, ConsentType consentType, String authResult) {
        return getRedirectUrl(state, QueryKeys.CODE, consentType.getCode())
                .queryParam(QueryKeys.RESULT, authResult)
                .get();
    }

    public ConsentResponse updateConsent(String consentId, UpdateConsentRequest body) {
        return createRequestInSession(Urls.CONSENTS.concat("/" + consentId))
                .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA)
                .put(ConsentResponse.class, body);
    }

    public ConsentResponse updateConsentPsuCredentials(
            String consentId, UpdateConsentPsuCredentialsRequest body) {
        return createRequestInSession(Urls.CONSENTS.concat("/" + consentId))
                .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA)
                .put(ConsentResponse.class, body);
    }

    public GetAccountsResponse getAccounts() {
        GetAccountsResponse getAccountsResponse =
                createAccountsRequestWithConsent().get(GetAccountsResponse.class);
        getAccountsResponse.getAccounts().removeIf(AccountEntity::isEmptyAccountObject);
        log.info(
                "Number of received checking accounts {}",
                getAccountsResponse.getAccounts().size());
        return getAccountsResponse;
    }

    public GetBalancesResponse getBalances(String resourceId) {
        return addPsuIpAddressHeaderIfNeeded(
                        createRequestWithConsent(
                                getBalancesUrl()
                                        .parameterNoEncoding(IdTags.ACCOUNT_ID, resourceId)))
                .get(GetBalancesResponse.class);
    }

    private URL getBalancesUrl() {
        return this.instrumentType.equals(InstrumentType.ACCOUNTS)
                ? Urls.BALANCES
                : Urls.CARD_BALANCES;
    }

    public GetTransactionsResponse getTransactions(
            String apiIdentifier,
            LocalDate fromDate,
            LocalDate toDate,
            String bookingType,
            int page) {
        HttpResponse response =
                addPsuIpAddressHeaderIfNeeded(
                                createRequestWithConsent(
                                                getTransactionsUrl()
                                                        .parameterNoEncoding(
                                                                IdTags.ACCOUNT_ID, apiIdentifier))
                                        .queryParam(QueryKeys.BOOKING_STATUS, bookingType)
                                        .queryParam(QueryKeys.DATE_FROM, fromDate.toString())
                                        .queryParam(QueryKeys.DATE_TO, toDate.toString())
                                        .queryParam(QueryKeys.OFFSET, String.valueOf(page)))
                        .get(HttpResponse.class);

        String totalPages = getTotalPages(response, apiIdentifier);

        temporaryStorage.putIfAbsent(apiIdentifier, totalPages);

        GetTransactionsResponse getTransactionsResponse =
                response.getBody(GetTransactionsResponse.class);

        if (Objects.nonNull(totalPages) && Integer.parseInt(totalPages) > page) {
            getTransactionsResponse.setPageRemaining(true);
        }
        return getTransactionsResponse;
    }

    private URL getTransactionsUrl() {
        return this.instrumentType.equals(InstrumentType.ACCOUNTS)
                ? Urls.TRANSACTIONS
                : Urls.CARD_TRANSACTIONS;
    }

    private String getTotalPages(HttpResponse response, String apiIdentifier) {
        return Optional.ofNullable(response.getHeaders().getFirst(QueryKeys.TOTAL_PAGES))
                .orElse(temporaryStorage.get(apiIdentifier));
    }

    public boolean isTokenValid() {
        return getTokenFromStorage().isValid();
    }

    public ConsentStatus getConsentStatus(String consentType) throws SessionException {
        return createRequestInSession(
                        Urls.CONSENTS_STATUS.parameter(
                                IdTags.CONSENT_ID, getConsentIdFromStorage(consentType)))
                .get(ConsentResponse.class)
                .getConsentStatus();
    }

    public ConsentDetailsResponse getConsentDetails(String consentType) throws SessionException {
        return createRequestInSession(
                        Urls.CONSENTS_STATUS.parameter(
                                IdTags.CONSENT_ID, getConsentIdFromStorage(consentType)))
                .get(ConsentDetailsResponse.class);
    }

    private String getConsentIdFromStorage(String consentType) throws SessionException {
        return persistentStorage
                .get(consentType, String.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, Payment payment) {
        RequestBuilder requestBuilder =
                createRequestInSession(
                                Urls.PAYMENT
                                        .parameter(
                                                PathParameterKeys.PAYMENT_SERVICE,
                                                getPaymentService(payment))
                                        .parameter(
                                                PathParameterKeys.PAYMENT_PRODUCT,
                                                getPaymentProduct(payment)))
                        .header(
                                HeaderKeys.ASPSP_PRODUCT_CODE,
                                providerConfiguration.getAspspProductCode())
                        .header(HeaderKeys.TPP_REDIRECT_PREFERRED, "true")
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                getRedirectUrl(
                                        sessionStorage.get(QueryKeys.STATE),
                                        HeaderKeys.CODE,
                                        HeaderValues.CODE));
        return addPsuIpAddressHeaderIfNeeded(requestBuilder)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    private URL getRedirectUrl(String s, String code, String code2) {
        return new URL(redirectUrl).queryParam(QueryKeys.STATE, s).queryParam(code, code2);
    }

    public CreatePaymentResponse getPayment(Payment payment) {
        RequestBuilder requestBuilder =
                createRequestInSession(
                                Urls.FETCH_PAYMENT
                                        .parameter(
                                                PathParameterKeys.PAYMENT_SERVICE,
                                                getPaymentService(payment))
                                        .parameter(
                                                PathParameterKeys.PAYMENT_PRODUCT,
                                                getPaymentProduct(payment))
                                        .parameter(IdTags.PAYMENT_ID, payment.getUniqueId()))
                        .header(
                                HeaderKeys.ASPSP_PRODUCT_CODE,
                                providerConfiguration.getAspspProductCode());
        return addPsuIpAddressHeaderIfNeeded(requestBuilder).get(CreatePaymentResponse.class);
    }

    public CreatePaymentResponse getPaymentStatus(Payment payment) {
        return createRequestInSession(
                        Urls.FETCH_PAYMENT_STATUS
                                .parameter(
                                        PathParameterKeys.PAYMENT_SERVICE,
                                        getPaymentService(payment))
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        getPaymentProduct(payment))
                                .parameter(IdTags.PAYMENT_ID, payment.getUniqueId()))
                .get(CreatePaymentResponse.class);
    }

    protected RequestBuilder addPsuIpAddressHeaderIfNeeded(RequestBuilder requestBuilder) {
        String originatingUserIPAddress =
                (psuIpAddress != null
                        ? psuIpAddress
                        : sessionStorage.get(HeaderKeys.PSU_IP_ADDRESS));
        return requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, originatingUserIPAddress);
    }

    private String getPaymentProduct(Payment payment) {
        return PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()
                ? PaymentProduct.INSTANT_SEPA_CREDIT_TRANSFERS
                : PaymentProduct.SEPA_CREDIT_TRANSFERS;
    }

    private String getPaymentService(Payment payment) {
        return PaymentServiceType.PERIODIC == payment.getPaymentServiceType()
                ? PathParameterValues.PAYMENT_SERVICE_PERIODIC_PAYMENTS
                : PathParameterValues.PAYMENT_SERVICE_PAYMENTS;
    }
}
