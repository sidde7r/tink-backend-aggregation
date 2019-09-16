package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.HttpErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.QueryValues.BookingStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Signature;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.GetConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.AccountBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.filters.ErrorFilter;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class RedsysApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private RedsysConfiguration configuration;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private X509Certificate clientSigningCertificate;
    private AspspConfiguration aspspConfiguration;
    private ConsentStatus cachedConsentStatus = ConsentStatus.UNKNOWN;
    private String psuIpAddress = null;
    private final EidasIdentity eidasIdentity;

    public RedsysApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            AspspConfiguration aspspConfiguration,
            EidasIdentity eidasIdentity) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.aspspConfiguration = aspspConfiguration;
        this.eidasIdentity = eidasIdentity;

        client.addFilter(
                new ErrorFilter(
                        HttpErrorCodes.TOO_MANY_REQUESTS,
                        ErrorCodes.ACCESS_EXCEEDED,
                        BankServiceError.ACCESS_EXCEEDED));
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
                getConfiguration().getBaseAuthUrl(), aspspConfiguration.getAspspCode(), path);
    }

    private String makeApiUrl(String path, Object... args) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (args.length > 0) {
            path = String.format(path, args);
        }
        return String.format(
                "%s/%s%s",
                getConfiguration().getBaseAPIUrl(), aspspConfiguration.getAspspCode(), path);
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
        final LocalDate consentValidUntil = LocalDate.now().plusDays(90);
        final GetConsentRequest getConsentRequest =
                new GetConsentRequest(
                        AccessEntity.ALL_PSD2,
                        FormValues.TRUE,
                        consentValidUntil,
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

    public ConsentResponse fetchConsent(String consentId) {
        final String url = makeApiUrl(Urls.CONSENT, consentId);
        return createSignedRequest(url).get(ConsentResponse.class);
    }

    public ConsentStatus fetchConsentStatus(String consentId) {
        // If valid, cache it for the session
        if (cachedConsentStatus == ConsentStatus.VALID) {
            return cachedConsentStatus;
        }
        final String url = makeApiUrl(Urls.CONSENT_STATUS, consentId);
        final ConsentStatusResponse consentStatusResponse =
                createSignedRequest(url).get(ConsentStatusResponse.class);
        cachedConsentStatus = consentStatusResponse.getConsentStatus();
        return cachedConsentStatus;
    }

    public OAuth2Token refreshToken(final String refreshToken) {
        final String url = makeAuthUrl(Urls.REFRESH);
        final String aspsp = aspspConfiguration.getAspspCode();
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
        if (!allHeaders.containsKey(HeaderKeys.REQUEST_ID)) {
            final String requestID = UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH);
            allHeaders.put(HeaderKeys.REQUEST_ID, requestID);
        }

        if (!Strings.isNullOrEmpty(psuIpAddress)) {
            allHeaders.put(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);
        }

        final String signature =
                RedsysUtils.generateRequestSignature(
                        configuration,
                        eidasProxyConfiguration,
                        clientSigningCertificate,
                        eidasIdentity,
                        allHeaders);
        allHeaders.put(HeaderKeys.SIGNATURE, signature);
        allHeaders.put(
                HeaderKeys.TPP_SIGNATURE_CERTIFICATE,
                RedsysUtils.getEncodedSigningCertificate(clientSigningCertificate));

        RequestBuilder request =
                client.request(url)
                        .addBearerToken(token)
                        .headers(allHeaders)
                        .accept(MediaType.APPLICATION_JSON);

        if (payload != null) {
            request = request.body(serializedPayload, MediaType.APPLICATION_JSON);
        }

        return request;
    }

    private String requestIdForAccount(String accountId) {
        String requestId = sessionStorage.get(StorageKeys.ACCOUNT_REQUEST_ID + accountId);
        if (Strings.isNullOrEmpty(requestId)) {
            requestId = UUID.randomUUID().toString().toLowerCase(Locale.ENGLISH);
            sessionStorage.put(StorageKeys.ACCOUNT_REQUEST_ID + accountId, requestId);
        }
        return requestId;
    }

    private void clearRequestIdForAccount(String accountId) {
        sessionStorage.remove(StorageKeys.ACCOUNT_REQUEST_ID + accountId);
    }

    public ListAccountsResponse fetchAccounts(String consentId) {
        RequestBuilder builder =
                createSignedRequest(makeApiUrl(Urls.ACCOUNTS))
                        .header(HeaderKeys.CONSENT_ID, consentId);
        if (aspspConfiguration.shouldRequestAccountsWithBalance()) {
            builder = builder.queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE);
        }
        return builder.get(ListAccountsResponse.class);
    }

    public AccountBalancesResponse fetchAccountBalances(String accountId, String consentId) {
        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.REQUEST_ID, requestIdForAccount(accountId));
        headers.put(HeaderKeys.CONSENT_ID, consentId);

        return createSignedRequest(makeApiUrl(Urls.BALANCES, accountId), null, headers)
                .get(AccountBalancesResponse.class);
    }

    private LocalDate transactionsFromDate(String accountId) {
        final Optional<LocalDate> fetchedDate = fetchedTransactionsUntil(accountId);
        final LocalDate defaultRefreshDate =
                LocalDate.now().minusDays(RedsysConstants.DEFAULT_REFRESH_DAYS);
        if (fetchedDate.isPresent() && fetchedDate.get().isAfter(defaultRefreshDate)) {
            return defaultRefreshDate;
        } else {
            // This might trigger SCA
            return aspspConfiguration.oldestTransactionDate();
        }
    }

    private Optional<LocalDate> fetchedTransactionsUntil(String accountId) {
        final String dateString =
                persistentStorage.get(StorageKeys.FETCHED_TRANSACTIONS_UNTIL + accountId);
        if (Objects.isNull(dateString)) {
            return Optional.empty();
        }
        return Optional.of(LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE));
    }

    private void setFetchingTransactionsUntil(String accountId, LocalDate date) {
        final String fetchedUntilDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        sessionStorage.put(StorageKeys.FETCHED_TRANSACTIONS_UNTIL + accountId, fetchedUntilDate);
    }

    private void persistFetchedTransactionsUntil(String accountId) {
        final String key = StorageKeys.FETCHED_TRANSACTIONS_UNTIL + accountId;
        final String value = sessionStorage.remove(key);
        if (!Objects.isNull(value)) {
            persistentStorage.put(key, value);
        }
    }

    private BaseTransactionsResponse fetchTransactions(String accountId, RequestBuilder request) {
        try {
            final BaseTransactionsResponse response =
                    request.get(aspspConfiguration.getTransactionsResponseClass());
            if (response.isLastPage()) {
                persistFetchedTransactionsUntil(accountId);
                clearRequestIdForAccount(accountId);
            }
            return response;
        } catch (HttpResponseException hre) {
            clearRequestIdForAccount(accountId);
            throw hre;
        }
    }

    public BaseTransactionsResponse fetchTransactions(
            String accountId, String consentId, LocalDate fromDate, LocalDate toDate) {
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.REQUEST_ID, requestIdForAccount(accountId));
        headers.put(HeaderKeys.CONSENT_ID, consentId);
        setFetchingTransactionsUntil(accountId, toDate);

        final RequestBuilder request =
                createSignedRequest(makeApiUrl(Urls.TRANSACTIONS, accountId), null, headers)
                        .queryParam(QueryKeys.DATE_FROM, formatter.format(fromDate))
                        .queryParam(QueryKeys.DATE_TO, formatter.format(toDate))
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BookingStatus.BOOKED);
        return fetchTransactions(accountId, request);
    }

    public BaseTransactionsResponse fetchPendingTransactions(String accountId, String consentId) {
        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.REQUEST_ID, requestIdForAccount(accountId));
        headers.put(HeaderKeys.CONSENT_ID, consentId);

        final RequestBuilder request =
                createSignedRequest(makeApiUrl(Urls.TRANSACTIONS, accountId), null, headers)
                        .queryParam(QueryKeys.BOOKING_STATUS, BookingStatus.PENDING);
        return fetchTransactions(accountId, request);
    }

    public BaseTransactionsResponse fetchTransactions(
            String accountId, String consentId, @Nullable String path) {
        if (path == null) {
            // Initial transactions request
            final LocalDate toDate = LocalDate.now();
            final LocalDate fromDate = transactionsFromDate(accountId);
            return fetchTransactions(accountId, consentId, fromDate, toDate);
        }

        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);
        headers.put(HeaderKeys.REQUEST_ID, requestIdForAccount(accountId));

        final RequestBuilder request = createSignedRequest(makeApiUrl(path), null, headers);
        return fetchTransactions(accountId, request);
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

    public void setPsuIpAddress(String psuIpAddress) {
        this.psuIpAddress = psuIpAddress;
    }
}
