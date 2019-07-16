package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.RedirectEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.StartAuthorizationProcessResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.utils.SparebankUtils;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SparebankApiClient {

    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    protected SparebankConfiguration configuration;

    public SparebankApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public URL getAuthorizeUrl(String state) {
        final URL redirectUrl =
                new URL(getConfiguration().getRedirectUrl()).queryParam("state", state);
        final String baseUrl = getConfiguration().getBaseUrl();

        Map<String, String> headers =
                new HashMap<String, String>() {
                    {
                        put(HeaderKeys.TPP_REDIRECT_URI.toLowerCase(), redirectUrl.toString());
                    }
                };

        RedirectEntity redirectEntity = null;
        try {
            String response =
                    createRequest(new URL(baseUrl + Urls.CONSENTS), headers).post(String.class);
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                redirectEntity = e.getResponse().getBody(RedirectEntity.class);
                if (redirectEntity.getLinks() == null
                        || redirectEntity.getLinks().getScaRedirect() == null
                        || redirectEntity.getLinks().getScaRedirect().getHref() == null) {
                    throw e;
                }
            } else {
                throw e;
            }
        }

        return new URL(redirectEntity.getLinks().getScaRedirect().getHref());
    }

    public AccountResponse fetchAccounts() {
        final String baseUrl = getConfiguration().getBaseUrl();
        Map<String, String> headers =
                new HashMap<String, String>() {
                    {
                        put(HeaderKeys.PSU_ID.toLowerCase(), getPsuId());
                        put(
                                HeaderKeys.TPP_REDIRECT_URI.toLowerCase(),
                                getConfiguration().getRedirectUrl());
                    }
                };
        return createRequestInSession(
                        new URL(baseUrl + SparebankConstants.Urls.FETCH_ACCOUNTS), headers)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(AccountResponse.class);
    }

    public TransactionResponse fetchTransactions(String resourceId, String offset, Integer limit) {
        final String baseUrl = getConfiguration().getBaseUrl();

        Map<String, String> headers =
                new HashMap<String, String>() {
                    {
                        put(HeaderKeys.PSU_ID.toLowerCase(), getPsuId());
                        put(
                                HeaderKeys.TPP_REDIRECT_URI.toLowerCase(),
                                getConfiguration().getRedirectUrl());
                    }
                };

        return createRequestInSession(
                        new URL(baseUrl + Urls.FETCH_TRANSACTIONS)
                                .parameter(IdTags.ACCOUNT_ID, resourceId),
                        headers)
                .queryParam(SparebankConstants.QueryKeys.LIMIT, Integer.toString(limit))
                .queryParam(SparebankConstants.QueryKeys.OFFSET, offset)
                .queryParam(
                        SparebankConstants.QueryKeys.BOOKING_STATUS,
                        SparebankConstants.QueryValues.BOOKING_STATUS)
                .get(TransactionResponse.class);
    }

    public CreatePaymentResponse createPayment(
            String paymentProduct, CreatePaymentRequest paymentRequest) {
        final String baseUrl = getConfiguration().getBaseUrl();
        final String redirectUrl = getConfiguration().getRedirectUrl();
        final String digest = "SHA-256=" + SparebankUtils.calculateDigest(paymentRequest.toData());

        Map<String, String> headers =
                new HashMap<String, String>() {
                    {
                        put(HeaderKeys.PSU_ID.toLowerCase(), getPsuId());
                        put(
                                HeaderKeys.TPP_REDIRECT_URI.toLowerCase(),
                                getConfiguration().getRedirectUrl());
                        put(HeaderKeys.DIGEST.toLowerCase(), digest);
                    }
                };

        return createRequestInSession(
                        new URL(baseUrl + Urls.CREATE_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct),
                        headers)
                .header(HeaderKeys.DIGEST, digest)
                .post(CreatePaymentResponse.class, paymentRequest);
    }

    public GetPaymentResponse getPayment(String paymentProduct, String paymentId) {
        final String baseUrl = getConfiguration().getBaseUrl();

        Map<String, String> headers =
                new HashMap<String, String>() {
                    {
                        put(HeaderKeys.PSU_ID.toLowerCase(), getPsuId());
                        put(
                                HeaderKeys.TPP_REDIRECT_URI.toLowerCase(),
                                getConfiguration().getRedirectUrl());
                    }
                };

        return createRequestInSession(
                        new URL(baseUrl + Urls.GET_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId),
                        headers)
                .get(GetPaymentResponse.class);
    }

    public StartAuthorizationProcessResponse startAuthorizationProcess(
            String paymentProduct, String paymetnId) {
        final String baseUrl = getConfiguration().getBaseUrl();

        URL redirectUrl =
                new URL(getConfiguration().getRedirectUrl())
                        .queryParam(StorageKeys.STATE, sessionStorage.get(StorageKeys.STATE));

        Map<String, String> headers =
                new HashMap<String, String>() {
                    {
                        put(HeaderKeys.PSU_ID.toLowerCase(), getPsuId());
                        put(HeaderKeys.TPP_REDIRECT_URI.toLowerCase(), redirectUrl.toString());
                    }
                };

        return createRequestInSession(
                        new URL(baseUrl + Urls.SIGN_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymetnId),
                        headers)
                .post(StartAuthorizationProcessResponse.class);
    }

    public GetPaymentStatusResponse getPaymentStatus(String paymentProduct, String paymentId) {
        final String baseUrl = getConfiguration().getBaseUrl();
        Map<String, String> headers =
                new HashMap<String, String>() {
                    {
                        put(HeaderKeys.PSU_ID.toLowerCase(), getPsuId());
                        put(
                                HeaderKeys.TPP_REDIRECT_URI.toLowerCase(),
                                getConfiguration().getRedirectUrl());
                    }
                };

        return createRequestInSession(
                        new URL(baseUrl + Urls.GET_PAYMENT_STATUS)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId),
                        headers)
                .get(GetPaymentStatusResponse.class);
    }

    protected RequestBuilder createRequest(URL url, Map<String, String> headers) {
        final String xRequestId = getXRequestId().toString();
        final String certificatePath = getConfiguration().getClientSigningCertificatePath();
        final String date = ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME);
        final String keyId = getConfiguration().getKeyId();
        final String keyPath = getConfiguration().getClientSigningKeyPath();
        final String tppId = getConfiguration().getTppId();
        final String psuIpAddress = getConfiguration().getPsuIpAdress();

        headers.put(
                HeaderKeys.DATE.toLowerCase(),
                ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        headers.put(HeaderKeys.X_REQUEST_ID.toLowerCase(), xRequestId);

        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.DATE, date)
                .header(HeaderKeys.TPP_ID, tppId)
                .header(HeaderKeys.X_REQUEST_ID, xRequestId)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        headers.get(HeaderKeys.TPP_REDIRECT_URI.toLowerCase()))
                .header(
                        HeaderKeys.TPP_SIGNATURE_CERTIFICATE,
                        SparebankUtils.getCertificateEncoded(certificatePath))
                .header(HeaderKeys.SIGNATURE, SparebankUtils.getSignature(headers, keyId, keyPath))
                .header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);
    }

    public RequestBuilder createRequestInSession(URL url, Map<String, String> headers) {
        return createRequest(url, headers)
                .header(HeaderKeys.TPP_SESSION_ID, getTppSessionId())
                .header(HeaderKeys.PSU_ID, getPsuId());
    }

    protected SparebankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(SparebankConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setUpTppSessionIdAndPsuId(String tppSessionId, String psuId) {
        sessionStorage.put(StorageKeys.TPP_SESSION_ID, tppSessionId);
        sessionStorage.put(StorageKeys.PSU_ID, psuId);
    }

    protected UUID getXRequestId() {
        return UUID.randomUUID();
    }

    protected String getPsuId() {
        return sessionStorage.get(StorageKeys.PSU_ID);
    }

    protected String getTppSessionId() {
        return sessionStorage.get(StorageKeys.TPP_SESSION_ID);
    }
}
