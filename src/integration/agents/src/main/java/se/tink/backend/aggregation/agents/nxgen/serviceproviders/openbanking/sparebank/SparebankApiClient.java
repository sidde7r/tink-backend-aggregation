package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HEADERS_TO_SIGN;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.StartAuthorizationProcessResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SparebankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final String baseUrl;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private String redirectUrl;
    private SparebankConfiguration configuration;
    private EidasIdentity eidasIdentity;

    public SparebankApiClient(
            TinkHttpClient client, SessionStorage sessionStorage, String baseUrl) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.baseUrl = baseUrl;
    }

    public void setConfiguration(
            final AgentConfiguration<SparebankConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration,
            EidasIdentity eidasIdentity) {
        this.configuration = agentConfiguration.getClientConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        client.setEidasProxy(eidasProxyConfiguration);
        this.eidasIdentity = eidasIdentity;
    }

    public void setPsuId(String psuId) {
        sessionStorage.put(StorageKeys.PSU_ID, psuId);
    }

    public void setTppSessionId(String tppSessionId) {
        sessionStorage.put(StorageKeys.SESSION_ID, tppSessionId);
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

    private RequestBuilder createRequest(URL url) {
        return createRequest(url, Optional.empty());
    }

    private RequestBuilder createRequest(URL url, Optional<String> digest) {
        Map<String, Object> headers = getHeaders(UUID.randomUUID().toString(), digest);
        headers.put(HeaderKeys.SIGNATURE, generateSignatureHeader(headers));
        return client.request(url).headers(headers);
    }

    public ScaResponse getScaRedirect(String state) throws HttpResponseException {
        sessionStorage.put(StorageKeys.STATE, state);
        return createRequest(new URL(getBaseUrl() + Urls.GET_SCA_REDIRECT)).get(ScaResponse.class);
    }

    public AccountResponse fetchAccounts() {
        return createRequest(new URL(getBaseUrl() + Urls.GET_ACCOUNTS))
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(AccountResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId, Date fromDate, Date toDate) {
        return createRequest(
                        new URL(getBaseUrl() + Urls.FETCH_TRANSACTIONS)
                                .parameter(IdTags.RESOURCE_ID, resourceId))
                .queryParam(
                        SparebankConstants.QueryKeys.DATE_FROM,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(
                        SparebankConstants.QueryKeys.DATE_TO,
                        ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(
                        SparebankConstants.QueryKeys.LIMIT,
                        SparebankConstants.QueryValues.TRANSACTION_LIMIT)
                .queryParam(
                        SparebankConstants.QueryKeys.BOOKING_STATUS,
                        SparebankConstants.QueryValues.BOOKING_STATUS)
                .get(TransactionResponse.class);
    }

    public CreatePaymentResponse createPayment(
            String paymentProduct, CreatePaymentRequest paymentRequest) {
        final String digest = "SHA-256=" + Hash.sha256Base64(paymentRequest.toData().getBytes());

        return createRequest(
                        new URL(baseUrl + Urls.CREATE_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct),
                        Optional.of(digest))
                .type(MediaType.APPLICATION_JSON)
                .post(CreatePaymentResponse.class, paymentRequest);
    }

    public GetPaymentResponse getPayment(String paymentProduct, String paymentId) {
        return createRequest(
                        new URL(baseUrl + Urls.GET_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }

    public StartAuthorizationProcessResponse startAuthorizationProcess(
            String paymentProduct, String paymentId) {

        return createRequest(
                        new URL(baseUrl + Urls.SIGN_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .post(StartAuthorizationProcessResponse.class);
    }

    public PaymentStatusResponse getPaymentStatus(String paymentProduct, String paymentId) {
        return createRequest(
                        new URL(baseUrl + Urls.GET_PAYMENT_STATUS)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(PaymentStatusResponse.class);
    }

    private Map<String, Object> getHeaders(String requestId, Optional<String> digest) {
        String tppRedirectUrl =
                new URL(redirectUrl)
                        .queryParam(QueryKeys.STATE, sessionStorage.get(StorageKeys.STATE))
                        .toString();

        Map<String, Object> headers = new HashMap<>();
        headers.put(HeaderKeys.ACCEPT, MediaType.APPLICATION_JSON);
        headers.put(HeaderKeys.TPP_ID, configuration.getTppId());
        headers.put(
                HeaderKeys.DATE, ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        headers.put(HeaderKeys.X_REQUEST_ID, requestId);
        headers.put(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl);
        headers.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, configuration.getCertificate());
        headers.put(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS);

        digest.ifPresent(digestString -> headers.put(HeaderKeys.DIGEST, digestString));
        getSessionId().ifPresent(sessionId -> headers.put(HeaderKeys.TPP_SESSION_ID, sessionId));
        getPsuId().ifPresent(psuId -> headers.put(HeaderKeys.PSU_ID, psuId));

        return headers;
    }

    private String generateSignatureHeader(Map<String, Object> headers) {
        QsealcSigner signer =
                QsealcSignerImpl.build(
                        eidasProxyConfiguration.toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);

        StringBuilder signedWithHeaderKeys = new StringBuilder();
        StringBuilder signedWithHeaderKeyValues = new StringBuilder();

        Arrays.stream(HEADERS_TO_SIGN.values())
                .map(HEADERS_TO_SIGN::getHeader)
                .filter(headers::containsKey)
                .forEach(
                        header -> {
                            signedWithHeaderKeyValues.append(
                                    String.format("%s: %s\n", header, headers.get(header)));
                            signedWithHeaderKeys.append(
                                    (signedWithHeaderKeys.length() == 0) ? header : " " + header);
                        });

        String signature =
                signer.getSignatureBase64(signedWithHeaderKeyValues.toString().trim().getBytes());

        String encodedSignature =
                Base64.getEncoder()
                        .encodeToString(
                                String.format(
                                                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"",
                                                configuration.getKeyId(),
                                                signedWithHeaderKeys.toString(),
                                                signature)
                                        .getBytes(StandardCharsets.UTF_8));

        return String.format("=?utf-8?B?%s?=", encodedSignature);
    }
}
