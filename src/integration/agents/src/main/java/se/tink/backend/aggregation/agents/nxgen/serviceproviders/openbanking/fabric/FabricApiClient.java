package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.CreateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.CreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.PaymentAuthorizationStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc.PaymentAuthorizationsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class FabricApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final RandomValueGenerator randomValueGenerator;
    private final SessionStorage sessionStorage;
    private String redirectUrl;
    private String baseUrl;

    FabricApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            RandomValueGenerator randomValueGenerator,
            SessionStorage sessionStorage,
            String baseUrl) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.randomValueGenerator = randomValueGenerator;
        this.sessionStorage = sessionStorage;
        this.baseUrl = baseUrl;
    }

    protected void setConfiguration(AgentConfiguration<FabricConfiguration> agentConfiguration) {
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID().toString())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String consentId = persistentStorage.get(StorageKeys.CONSENT_ID);
        return createRequest(url).header(HeaderKeys.CONSENT_ID, consentId);
    }

    public CreateConsentResponse getConsent(String state) {
        final URL redirectUri = new URL(redirectUrl).queryParam(QueryKeys.STATE, state);

        return client.request(new URL(baseUrl + Urls.CONSENT))
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID().toString())
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUri.toString())
                .header(HeaderKeys.TPP_REDIRECT_PREFERED, HeaderValues.TPP_REDIRECT_PREFERED)
                .post(CreateConsentResponse.class, new CreateConsentRequest());
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return createRequest(
                        new URL(baseUrl + Urls.GET_CONSENT_STATUS)
                                .parameter(IdTags.CONSENT_ID, consentId))
                .get(ConsentStatusResponse.class);
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return createRequest(
                        new URL(baseUrl + Urls.GET_CONSENT_DETAILS)
                                .parameter(IdTags.CONSENT_ID, consentId))
                .get(ConsentDetailsResponse.class);
    }

    public AccountResponse fetchAccounts() {
        return createRequestInSession(new URL(baseUrl + Urls.GET_ACCOUNTS))
                .get(AccountResponse.class);
    }

    public BalanceResponse getBalances(String url) {
        return createRequestInSession(new URL(baseUrl + Urls.API_PSD2_URL + url))
                .get(BalanceResponse.class);
    }

    public AccountDetailsResponse getAccountDetails(String url) {
        return createRequestInSession(new URL(baseUrl + Urls.API_PSD2_URL + url))
                .get(AccountDetailsResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId, Date fromDate, Date toDate) {
        return createRequestInSession(
                        new URL(baseUrl + Urls.GET_TRANSACTIONS)
                                .parameter(IdTags.ACCOUNT_ID, resourceId))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(TransactionResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return client.request(
                        new URL(Urls.INITIATE_A_PAYMENT_URL)
                                .parameter(
                                        FabricConstants.PathParameterKeys.PAYMENT_PRODUCT,
                                        sessionStorage.get(
                                                FabricConstants.PathParameterKeys.PAYMENT_PRODUCT)))
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.TPP_REDIRECT_PREFERED, HeaderValues.TPP_REDIRECT_PREFERED)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, getPsuIpAddress())
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(redirectUrl)
                                .queryParam(QueryKeys.CODE, FabricConstants.QueryValues.CODE)
                                .queryParam(QueryKeys.STATE, sessionStorage.get(QueryKeys.STATE)))
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public CreatePaymentResponse getPayment(String paymentId) {
        return client.request(
                        new URL(Urls.GET_PAYMENT_URL)
                                .parameter(
                                        FabricConstants.PathParameterKeys.PAYMENT_PRODUCT,
                                        sessionStorage.get(
                                                FabricConstants.PathParameterKeys.PAYMENT_PRODUCT))
                                .parameter(FabricConstants.PathParameterKeys.PAYMENT_ID, paymentId))
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, getPsuIpAddress())
                .get(CreatePaymentResponse.class);
    }

    public CreatePaymentResponse getPaymentStatus(String paymentId) {
        return client.request(
                        new URL(Urls.GET_PAYMENT_STATUS_URL)
                                .parameter(
                                        FabricConstants.PathParameterKeys.PAYMENT_PRODUCT,
                                        sessionStorage.get(
                                                FabricConstants.PathParameterKeys.PAYMENT_PRODUCT))
                                .parameter(FabricConstants.PathParameterKeys.PAYMENT_ID, paymentId))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, getPsuIpAddress())
                .get(CreatePaymentResponse.class);
    }

    public PaymentAuthorizationsResponse getPaymentAuthorizations(String paymentId) {
        PaymentAuthorizationsResponse result =
                client.request(
                                new URL(Urls.GET_PAYMENT_AUTHORIZATIONS_URL)
                                        .parameter(
                                                FabricConstants.PathParameterKeys.PAYMENT_PRODUCT,
                                                sessionStorage.get(
                                                        FabricConstants.PathParameterKeys
                                                                .PAYMENT_PRODUCT))
                                        .parameter(
                                                FabricConstants.PathParameterKeys.PAYMENT_ID,
                                                paymentId))
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                        .header(HeaderKeys.PSU_IP_ADDRESS, getPsuIpAddress())
                        .get(PaymentAuthorizationsResponse.class);
        if (!result.getAuthorisationIds().isEmpty()) {
            sessionStorage.put(
                    StorageKeys.PAYMENT_AUTHORIZATION_ID, result.getLastAuthorisationId());
        }
        return result;
    }

    public PaymentAuthorizationStatus getPaymentAuthorizationStatus(String paymentId) {
        return client.request(
                        new URL(Urls.GET_PAYMENT_AUTHORIZATION_STATUS_URL)
                                .parameter(
                                        FabricConstants.PathParameterKeys.PAYMENT_PRODUCT,
                                        sessionStorage.get(
                                                FabricConstants.PathParameterKeys.PAYMENT_PRODUCT))
                                .parameter(FabricConstants.PathParameterKeys.PAYMENT_ID, paymentId)
                                .parameter(
                                        FabricConstants.PathParameterKeys.PAYMENT_AUTHORIZATION_ID,
                                        sessionStorage.get(StorageKeys.PAYMENT_AUTHORIZATION_ID)))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, getPsuIpAddress())
                .get(PaymentAuthorizationStatus.class);
    }

    private String getPsuIpAddress() {
        return Optional.ofNullable(sessionStorage.get(HeaderKeys.PSU_IP_ADDRESS))
                .orElse(HeaderValues.DUMMY_IP_ADDRESS);
    }
}
