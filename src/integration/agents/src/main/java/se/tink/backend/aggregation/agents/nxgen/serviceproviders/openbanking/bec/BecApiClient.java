package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;

import static se.tink.libraries.serialization.utils.SerializationUtils.serializeToString;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.SignatureUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.ApiService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.PaymentTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.configuration.BecConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class BecApiClient {
    private final TinkHttpClient client;
    private String state;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private BecConfiguration configuration;
    private AgentsServiceConfiguration config;

    public BecApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    public BecConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(
            BecConfiguration becConfiguration, final AgentsServiceConfiguration configuration) {
        this.configuration = becConfiguration;
        this.config = configuration;
    }

    public RequestBuilder createRequest(URL url, String requestBody) {
        String xRequestId = UUID.randomUUID().toString();
        String digest = SignatureUtils.createDigest(requestBody);
        String redirectUri =
                new URL(HeaderValues.TPP_REDIRECT_URI).queryParam("state", state).toString();
        String signature = createSignature(xRequestId, digest, redirectUri);

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID))
                .header(HeaderKeys.PSU_IP, HeaderValues.PSU_IP)
                .header(HeaderKeys.X_REQUEST_ID, xRequestId)
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUri)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, redirectUri)
                .header(HeaderKeys.DIGEST, digest)
                .header(HeaderKeys.SIGNATURE, signature)
                .header(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, HeaderValues.TPP_CERTIFICATE);
    }

    private String createSignature(String uuid, String digest, String redirectUri) {
        QsealcEidasProxySigner qsealSigner =
                new QsealcEidasProxySigner(config.getEidasProxy(), "Tink");

        String consentSignatureString =
                String.format(
                        "x-request-id: %s\n" + HeaderKeys.TPP_REDIRECT_URI + ": %s\ndigest: %s",
                        uuid,
                        redirectUri,
                        digest);
        String signedB64Signature =
                qsealSigner.getSignatureBase64(consentSignatureString.getBytes());
        return String.format(
                "keyId=\"%s\", algorithm=\"rsa-sha256\", headers=\"x-request-id tpp-redirect-uri digest\", signature=\"%s\"",
                HeaderValues.CERTIFICATE_KEY_ID, signedB64Signature);
    }

    public ConsentResponse getConsent(String state) {
        this.state = state;
        ConsentRequest body = createConsentRequestBody();
        ConsentResponse response =
                createRequest(Urls.GET_CONSENT, serializeToString(body)).body(body).post(ConsentResponse.class);
        persistentStorage.put(StorageKeys.CONSENT_ID, response.getConsentId());
        return response;
    }

    public ConsentResponse getConsentStatus() {
        return createRequest(new URL(Urls.BASE_URL + ApiService.GET_CONSENT_STATUS)
                                        .parameter(StorageKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID)),
            FormValues.EMPTY_STRING)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                        .get(ConsentResponse.class);
    }

    public ConsentRequest createConsentRequestBody() {
        AccessEntity access = new AccessEntity("allAccounts");
        ConsentRequest consentRequest =
                new ConsentRequest(access, "false", "2019-09-01", "true", 4);
        return consentRequest;
    }

    public List<TransactionalAccount> getAccounts() {
        return createRequest(Urls.GET_ACCOUNTS, FormValues.EMPTY_STRING)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(GetAccountsResponse.class)
                .toTinkAccounts();
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final URL url =
                Urls.GET_TRANSACTIONS.parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return createRequest(url, FormValues.EMPTY_STRING)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return createRequest(
                        se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec
                                .BecConstants.Urls.CREATE_PAYMENT
                                .parameter(
                                        se.tink.backend.aggregation.agents.nxgen.serviceproviders
                                                .openbanking.bec.BecConstants.IdTags.PAYMENT_TYPE,
                                        PaymentTypes.INSTANT_DANISH_DOMESTIC_CREDIT_TRANSFER),
            FormValues.EMPTY_STRING)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(String paymentId) {
        return createRequest(
                        se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec
                                .BecConstants.Urls.GET_PAYMENT
                                .parameter(
                                        se.tink.backend.aggregation.agents.nxgen.serviceproviders
                                                .openbanking.bec.BecConstants.IdTags.PAYMENT_ID,
                                        paymentId),
            FormValues.EMPTY_STRING)
                .get(GetPaymentResponse.class);
    }
}
