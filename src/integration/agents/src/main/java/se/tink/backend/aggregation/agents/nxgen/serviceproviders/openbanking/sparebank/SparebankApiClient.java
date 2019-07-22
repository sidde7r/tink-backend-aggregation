package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.StartAuthorizationProcessResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.utils.SparebankUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidas.QsealcEidasProxySigner;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SparebankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final String baseUrl;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private SparebankConfiguration configuration;

    public SparebankApiClient(
            TinkHttpClient client, SessionStorage sessionStorage, String baseUrl) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.baseUrl = baseUrl;
    }

    public void setConfiguration(
            final SparebankConfiguration configuration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = configuration;
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        client.setEidasProxy(eidasProxyConfiguration, configuration.getEidasCertId());
    }

    public void setUpTppSessionIdAndPsuId(String tppSessionId, String psuId) {
        sessionStorage.put(StorageKeys.SESSION_ID, tppSessionId);
        sessionStorage.put(StorageKeys.PSU_ID, psuId);
    }

    private Optional<String> getPsuId() {
        return Optional.ofNullable(sessionStorage.get(StorageKeys.PSU_ID));
    }

    private Optional<String> getSessionId() {
        return Optional.ofNullable(sessionStorage.get(StorageKeys.SESSION_ID));
    }

    private String getBaseUrl() {
        return baseUrl;
    }

    private RequestBuilder createRequest(URL url, Optional<String> digest) {
        Map<String, Object> headers = getHeaders(UUID.randomUUID().toString(), digest);
        headers.put(QueryParams.SIGNATURE, generateSignatureHeader(headers));
        return client.request(url).headers(headers);
    }

    public ScaResponse getScaRedirect(String state) throws HttpResponseException {
        sessionStorage.put(StorageKeys.STATE, state);
        return createRequest(new URL(getBaseUrl() + Urls.GET_SCA_REDIRECT), Optional.empty())
                .get(ScaResponse.class);
    }

    public AccountResponse fetchAccounts() {
        return createRequest(new URL(getBaseUrl() + Urls.GET_ACCOUNTS), Optional.empty())
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(AccountResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId, String offset, Integer limit) {
        return createRequest(
                        new URL(getBaseUrl() + Urls.FETCH_TRANSACTIONS)
                                .parameter(IdTags.RESOURCE_ID, resourceId),
                        Optional.empty())
                .queryParam(SparebankConstants.QueryKeys.LIMIT, Integer.toString(limit))
                .queryParam(SparebankConstants.QueryKeys.OFFSET, offset)
                .queryParam(
                        SparebankConstants.QueryKeys.BOOKING_STATUS,
                        SparebankConstants.QueryValues.BOOKING_STATUS)
                .get(TransactionResponse.class);
    }

    public CreatePaymentResponse createPayment(
            String paymentProduct, CreatePaymentRequest paymentRequest) {
        final String digest = "SHA-256=" + SparebankUtils.calculateDigest(paymentRequest.toData());

        return createRequest(
                        new URL(baseUrl + Urls.CREATE_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct),
                        Optional.of(digest))
                .header(HeaderKeys.DIGEST, digest)
                .post(CreatePaymentResponse.class, paymentRequest);
    }

    public GetPaymentResponse getPayment(String paymentProduct, String paymentId) {
        return createRequest(
                        new URL(baseUrl + Urls.GET_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId),
                        Optional.empty())
                .get(GetPaymentResponse.class);
    }

    public StartAuthorizationProcessResponse startAuthorizationProcess(
            String paymentProduct, String paymetnId) {

        return createRequest(
                        new URL(baseUrl + Urls.SIGN_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymetnId),
                        Optional.empty())
                .post(StartAuthorizationProcessResponse.class);
    }

    public GetPaymentStatusResponse getPaymentStatus(String paymentProduct, String paymentId) {
        return createRequest(
                        new URL(baseUrl + Urls.GET_PAYMENT_STATUS)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId),
                        Optional.empty())
                .get(GetPaymentStatusResponse.class);
    }

    private Map<String, Object> getHeaders(String requestId, Optional<String> digest) {
        String redirectUrl =
                new URL(configuration.getRedirectUrl())
                        .queryParam("state", sessionStorage.get(StorageKeys.STATE))
                        .toString();

        Map<String, String> headers =
                new HashMap<String, String>() {
                    {
                        put("Accept", MediaType.APPLICATION_JSON);
                        put(QueryParams.TPP_ID, configuration.getTppId());
                        put(
                                "Date",
                                ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
                        put(QueryParams.X_REQUEST_ID, requestId);
                        put(QueryParams.TPP_REDIRECT_URI, redirectUrl);
                        put(QueryParams.TPP_SIGNATURE_CERTIFICATE, configuration.getBase64Pem());
                        put(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS);
                    }
                };

        digest.ifPresent(digestString -> headers.put(HeaderKeys.DIGEST, digestString));
        getSessionId().ifPresent(sessionId -> headers.put(HeaderKeys.TPP_SESSION_ID, sessionId));
        getPsuId().ifPresent(psuId -> headers.put(HeaderKeys.PSU_ID, psuId));

        return headers.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey().toLowerCase(),
                                entry -> String.valueOf(entry.getValue())));
    }

    private String generateSignatureHeader(Map<String, Object> headers) {
        QsealcEidasProxySigner signer = new QsealcEidasProxySigner(eidasProxyConfiguration, "evry");
        String[] headersToSign =
                new String[] {
                    "date",
                    "digest",
                    "x-request-id",
                    "psu-id",
                    "psu-corporate-id",
                    "tpp-redirect-uri"
                };
        StringBuilder signedWithHeaderKeys = new StringBuilder();
        StringBuilder signedWithHeaderKeyValues = new StringBuilder();

        for (int i = 0; i <= headersToSign.length - 1; i++) {
            String fieldName = headersToSign[i];
            if (headers.containsKey(fieldName)) {
                Object value = headers.get(fieldName);
                signedWithHeaderKeyValues.append(String.format("%s: %s\n", fieldName, value));
                signedWithHeaderKeys.append(
                        (signedWithHeaderKeys.length() == 0) ? fieldName : " " + fieldName);
            }
        }

        String signature =
                signer.getSignatureBase64(signedWithHeaderKeyValues.toString().trim().getBytes());

        return String.format(
                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"",
                configuration.getKeyId(), signedWithHeaderKeys.toString(), signature);
    }
}
