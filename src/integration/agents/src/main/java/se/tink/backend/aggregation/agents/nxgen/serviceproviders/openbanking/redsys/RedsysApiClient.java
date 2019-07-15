package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.GetConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class RedsysApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private RedsysConfiguration configuration;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private RedsysConsentController consentController;
    private X509Certificate clientSigningCertificate;

    public RedsysApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.consentController =
                new RedsysConsentController(this, sessionStorage, supplementalInformationHelper);
    }

    private RedsysConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(
            RedsysConfiguration configuration, EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = configuration;
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        this.clientSigningCertificate =
                RedsysUtils.parseCertificate(configuration.getClientSigningCertificate());

        if (eidasProxyConfiguration != null && configuration.getCertificateId() != null) {
            client.setEidasProxy(eidasProxyConfiguration, configuration.getCertificateId());
        }
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public boolean hasValidAccessToken() {
        try {
            return getTokenFromStorage().isValid();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private String makeAuthUrl(String path) {
        assert path.startsWith("/");
        return String.format(
                "%s/%s%s",
                getConfiguration().getBaseAuthUrl(), getConfiguration().getAspsp(), path);
    }

    private String makeApiUrl(String path, Object... args) {
        assert path.startsWith("/");
        if (args.length > 0) {
            path = String.format(path, args);
        }
        return String.format(
                "%s/%s%s", getConfiguration().getBaseAPIUrl(), getConfiguration().getAspsp(), path);
    }

    public URL getAuthorizeUrl(String state, String codeChallenge) {
        final String clientId = getAuthClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        return client.request(makeAuthUrl(RedsysConstants.Urls.OAUTH))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    public OAuth2Token getToken(String code, String codeVerifier) {
        final String clientId = getAuthClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        final String payload =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.CLIENT_ID, clientId)
                        .put(FormKeys.CODE, code)
                        .put(FormKeys.REDIRECT_URI, redirectUri)
                        .put(FormKeys.CODE_VERIFIER, codeVerifier)
                        .build()
                        .serialize();

        final OAuth2Token token =
                client.request(makeAuthUrl(Urls.TOKEN))
                        .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(TokenResponse.class)
                        .toTinkToken();
        return token;
    }

    public Pair<String, URL> requestConsent(String scaState) {
        final String url = makeApiUrl(Urls.CONSENTS);
        final GetConsentRequest getConsentRequest =
                new GetConsentRequest(
                        new AccessEntity(null, null, null, null, FormValues.ALL_ACCOUNTS),
                        FormValues.TRUE,
                        FormValues.VALID_UNTIL,
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.FALSE);

        final GetConsentResponse getConsentResponse =
                createSignedRequest(url, getConsentRequest, getTppRedirectHeaders(scaState))
                        .post(GetConsentResponse.class);
        final String consentId = getConsentResponse.getConsentId();
        final String consentRedirectUrl =
                getConsentResponse
                        .getLink(RedsysConstants.Links.SCA_REDIRECT)
                        .map(LinkEntity::getHref)
                        .get();
        return new Pair<>(consentId, new URL(consentRedirectUrl));
    }

    private String getConsentId() {
        return sessionStorage.get(StorageKeys.CONSENT_ID);
    }

    public String fetchConsentStatus(String consentId) {
        final String url = makeApiUrl(Urls.CONSENT_STATUS, consentId);
        final ConsentStatusResponse consentStatusResponse =
                createSignedRequest(url).get(ConsentStatusResponse.class);
        return consentStatusResponse.getConsentStatus();
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final String url = getConfiguration().getBaseAuthUrl() + "/" + Urls.REFRESH;
        final String aspsp = getConfiguration().getAspsp();
        final String clientId = getAuthClientId();

        final String payload =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                        .put(FormKeys.ASPSP, aspsp)
                        .put(FormKeys.CLIENT_ID, clientId)
                        .put(FormKeys.REFRESH_TOKEN, refreshToken)
                        .build()
                        .serialize();

        return client.request(url)
                .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    private String getAuthClientId() {
        return RedsysUtils.getAuthClientId(clientSigningCertificate);
    }

    private Map<String, Object> getTppRedirectHeaders(String state) {
        final URL redirectUrl =
                new URL(getConfiguration().getRedirectUrl()).queryParam(QueryKeys.STATE, state);
        Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.TPP_REDIRECT_PREFERRED, HeaderValues.TRUE);
        headers.put(
                HeaderKeys.TPP_REDIRECT_URI,
                redirectUrl.queryParam(QueryKeys.OK, QueryValues.TRUE));
        headers.put(
                HeaderKeys.TPP_NOK_REDIRECT_URI,
                redirectUrl.queryParam(QueryKeys.OK, QueryValues.FALSE));
        return headers;
    }

    private RequestBuilder createSignedRequest(
            String url, @Nullable Object payload, Map<String, Object> headers) {
        return createSignedRequest(url, payload, getTokenFromStorage(), headers);
    }

    private RequestBuilder createSignedRequest(String url, @Nullable Object payload) {
        return createSignedRequest(url, payload, getTokenFromStorage(), Maps.newHashMap());
    }

    private RequestBuilder createSignedRequest(String url) {
        return createSignedRequest(url, null, getTokenFromStorage(), Maps.newHashMap());
    }

    private RequestBuilder createSignedRequest(
            String url, @Nullable Object payload, OAuth2Token token, Map<String, Object> headers) {
        String serializedPayload = "";
        if (payload != null) {
            serializedPayload = SerializationUtils.serializeToString(payload);
        }

        // construct headers
        final Map<String, Object> allHeaders = Maps.newHashMap(headers);
        allHeaders.put(HeaderKeys.IBM_CLIENT_ID, getConfiguration().getClientId());
        final String digest =
                Signature.DIGEST_PREFIX
                        + Base64.getEncoder().encodeToString(Hash.sha256(serializedPayload));
        allHeaders.put(HeaderKeys.DIGEST, digest);
        final String requestID = UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH);
        allHeaders.put(HeaderKeys.REQUEST_ID, requestID);
        final String signature =
                RedsysUtils.generateRequestSignature(
                        configuration,
                        eidasProxyConfiguration,
                        clientSigningCertificate,
                        allHeaders);
        allHeaders.put(HeaderKeys.SIGNATURE, signature);
        allHeaders.put(
                HeaderKeys.TPP_SIGNATURE_CERTIFICATE,
                RedsysUtils.getEncodedSigningCertificate(clientSigningCertificate));

        RequestBuilder request = client.request(url).addBearerToken(token).headers(allHeaders);

        if (payload != null) {
            request = request.body(serializedPayload, MediaType.APPLICATION_JSON);
        }

        return request;
    }

    public ListAccountsResponse fetchAccounts() {
        consentController.askForConsentIfNeeded();
        final String consentId = getConsentId();
        return createSignedRequest(makeApiUrl(Urls.ACCOUNTS))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                .get(ListAccountsResponse.class);
    }

    public TransactionsResponse fetchTransactions(String accountId, @Nullable String link) {
        consentController.askForConsentIfNeeded();
        final String consentId = getConsentId();
        final String path =
                Optional.ofNullable(link).orElse(String.format(Urls.TRANSACTIONS, accountId));
        RequestBuilder request =
                createSignedRequest(makeApiUrl(path))
                        .header(HeaderKeys.CONSENT_ID, consentId)
                        .queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BookingStatus.BOTH);

        return request.get(TransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest request, PaymentProduct paymentProduct, String scaToken) {
        final String url = makeApiUrl(Urls.CREATE_PAYMENT, paymentProduct.getProductName());
        return createSignedRequest(url, request, getTppRedirectHeaders(scaToken))
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .post(CreatePaymentResponse.class);
    }

    public GetPaymentResponse fetchPayment(String paymentId, PaymentProduct paymentProduct) {
        final String url = makeApiUrl(Urls.GET_PAYMENT, paymentProduct.getProductName(), paymentId);
        return createSignedRequest(url)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(GetPaymentResponse.class);
    }

    public PaymentStatusResponse fetchPaymentStatus(
            String paymentId, PaymentProduct paymentProduct) {
        final String url =
                makeApiUrl(Urls.PAYMENT_STATUS, paymentProduct.getProductName(), paymentId);
        return createSignedRequest(url)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(PaymentStatusResponse.class);
    }

    public void cancelPayment(String paymentId, PaymentProduct paymentProduct) {
        final String url =
                makeApiUrl(Urls.PAYMENT_CANCEL, paymentProduct.getProductName(), paymentId);
        createSignedRequest(url).delete();
    }
}
