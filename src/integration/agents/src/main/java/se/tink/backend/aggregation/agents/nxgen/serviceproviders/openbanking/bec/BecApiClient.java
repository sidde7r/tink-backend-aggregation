package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;


import static se.tink.libraries.serialization.utils.SerializationUtils.serializeToString;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.ApiService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.HeadersToSign;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.PaymentTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.configuration.BecConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class BecApiClient {
    private final TinkHttpClient client;
    private String state;
    private final String baseUrl;
    private final PersistentStorage persistentStorage;
    private BecConfiguration becConfiguration;
    private AgentsServiceConfiguration config;

    public BecApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, String baseUrl) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.baseUrl = baseUrl;
    }

    public void setConfiguration(
            BecConfiguration becConfiguration, final AgentsServiceConfiguration configuration) {
        this.becConfiguration = becConfiguration;
        this.config = configuration;
    }

    private Map<String, Object> getHeaders(String requestId, String digest) {
        String redirectUrl =
                new URL(becConfiguration.getRedirectUrl())
                        .queryParam(QueryKeys.STATE, state)
                        .toString();

        Map<String, Object> headers =
                new HashMap<String, Object>() {
                    {
                        put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
                        put(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
                        put(HeaderKeys.PSU_IP, HeaderValues.PSU_IP);
                        put(HeaderKeys.X_REQUEST_ID, requestId);
                        put(HeaderKeys.TPP_REDIRECT_URI, redirectUrl);
                        put(HeaderKeys.TPP_NOK_REDIRECT_URI, redirectUrl);
                        put(HeaderKeys.DIGEST, digest);
                        put(
                                HeaderKeys.TPP_SIGNATURE_CERTIFICATE,
                                becConfiguration.getQsealCertificate());
                    }
                };

        return headers;
    }

    public RequestBuilder createRequest(URL url, String requestBody) {
        String requestId = UUID.randomUUID().toString();
        String digest = createDigest(requestBody);
        Map<String, Object> headers = getHeaders(requestId, digest);

        return client.request(url)
            .type(MediaType.APPLICATION_JSON)
            .headers(headers)
            .header(HeaderKeys.SIGNATURE, generateSignatureHeader(headers));
    }
    public RequestBuilder createRequest(URL url) {
        return createRequest(url, FormValues.EMPTY_STRING);
    }

    public ConsentResponse getConsent(String state) throws HttpResponseException {
        this.state = state;
        ConsentRequest body = createConsentRequestBody();
        ConsentResponse response =
                createRequest(new URL(baseUrl.concat(ApiService.GET_CONSENT)), serializeToString(body))
                        .body(body)
                        .post(ConsentResponse.class);
        persistentStorage.put(StorageKeys.CONSENT_ID, response.getConsentId());
        return response;
    }

    public ConsentResponse getConsentStatus() {
        return createRequest(
                        new URL(baseUrl.concat(ApiService.GET_CONSENT_STATUS))
                                .parameter(
                                        StorageKeys.CONSENT_ID,
                                        persistentStorage.get(StorageKeys.CONSENT_ID)))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .get(ConsentResponse.class);
    }

    public ConsentRequest createConsentRequestBody() {
        return new ConsentRequest(
            new AccessEntity(
                FormValues.ACCESS_TYPE),
                FormValues.FALSE,
                FormValues.VALID_UNTIL,
                FormValues.TRUE,
                FormValues.FREQUENCY_PER_DAY);
    }

    public GetAccountsResponse getAccounts() {
        return createRequest(new URL(baseUrl.concat(ApiService.GET_ACCOUNTS)))
                .get(GetAccountsResponse.class);
    }

    public BalancesResponse getBalances(AccountEntity account) {
        return createRequest(
                        new URL(baseUrl.concat(ApiService.GET_BALANCES))
                                .parameter(IdTags.ACCOUNT_ID, account.getResourceId()))
                .get(BalancesResponse.class);
    }

    public PaginatorResponse getTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final URL url =
                new URL(baseUrl.concat(ApiService.GET_TRANSACTIONS))
                        .parameter(IdTags.ACCOUNT_ID, account.getApiIdentifier());

        return createRequest(url)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .get(GetTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {
        return createRequest(
                        new URL(baseUrl.concat(ApiService.CREATE_PAYMENT))
                                .parameter(
                                        IdTags.PAYMENT_TYPE,
                                        PaymentTypes.INSTANT_DANISH_DOMESTIC_CREDIT_TRANSFER))
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(String paymentId) {
        return createRequest(
                        new URL(baseUrl.concat(ApiService.GET_PAYMENT))
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }

    private String generateSignatureHeader(Map<String, Object> headers) {
        QsealcEidasProxySigner signer =
            new QsealcEidasProxySigner(config.getEidasProxy(), becConfiguration.getEidasQwac());

        String signedHeaders =
            Arrays.stream(HeadersToSign.values())
                .map(HeadersToSign::getHeader)
                .filter(headers::containsKey)
                .collect(Collectors.joining(" "));

        String signedHeadersWithValues =
            Arrays.stream(HeadersToSign.values())
                .map(HeadersToSign::getHeader)
                .filter(headers::containsKey)
                .map(header -> String.format("%s: %s", header, headers.get(header)))
                .collect(Collectors.joining("\n"));

        String signature = signer.getSignatureBase64(signedHeadersWithValues.getBytes());

        return String.format(
            BecConstants.HeaderValues.SIGNATURE_HEADER,
            becConfiguration.getKeyId(),
            signedHeaders,
            signature);
    }



    private String createDigest(String body) {
        return String.format(HeaderValues.SHA_256.concat("%s"),Base64.getEncoder().encodeToString(Hash.sha256(body)));
    }
}
