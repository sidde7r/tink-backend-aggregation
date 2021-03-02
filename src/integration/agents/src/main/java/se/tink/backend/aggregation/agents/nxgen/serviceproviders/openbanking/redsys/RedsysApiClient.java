package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.security.cert.CertificateException;
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
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.AccountBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.filters.ErrorFilter;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateBusinessScope;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils.CANameEncoding;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class RedsysApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private RedsysConfiguration configuration;
    private String redirectUrl;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private AspspConfiguration aspspConfiguration;
    private String authClientId;
    private String clientSigningCertificate;
    private String signingKeyId;
    private String authScopes;
    private ConsentStatus cachedConsentStatus = ConsentStatus.UNKNOWN;
    private String psuIpAddress = null;
    private final EidasIdentity eidasIdentity;
    private final CredentialsRequest request;

    public RedsysApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            AspspConfiguration aspspConfiguration,
            EidasIdentity eidasIdentity,
            CredentialsRequest request) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.aspspConfiguration = aspspConfiguration;
        this.eidasIdentity = eidasIdentity;
        this.request = request;

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

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(
            AgentConfiguration<RedsysConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        try {
            this.authClientId =
                    CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQsealc());
            this.clientSigningCertificate =
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                            agentConfiguration.getQsealc());
            this.signingKeyId =
                    Psd2Headers.getTppCertificateKeyId(
                            agentConfiguration.getQsealc(), 16, CANameEncoding.BASE64_IF_NOT_ASCII);
            this.authScopes =
                    CertificateUtils.getBusinessScopeFromCertificate(agentConfiguration.getQsealc())
                                    .contains(CertificateBusinessScope.PIS)
                            ? QueryValues.SCOPE
                            : QueryValues.AIS_SCOPE;
        } catch (CertificateException | IOException e) {
            throw new IllegalStateException("Could not read values from QsealC certificate", e);
        }

        if (eidasProxyConfiguration != null) {
            client.setEidasProxy(eidasProxyConfiguration);
        }
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    private String makeAuthUrl(String path) {
        assert path.startsWith("/");
        return String.format(
                "%s/%s%s", Urls.BASE_AUTH_URL, aspspConfiguration.getAspspCode(), path);
    }

    private String makeApiUrl(String path, Object... args) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (args.length > 0) {
            path = String.format(path, args);
        }
        return String.format("%s/%s%s", Urls.BASE_API_URL, aspspConfiguration.getAspspCode(), path);
    }

    public URL getAuthorizeUrl(String state, String codeChallenge) {
        final String redirectUri = getRedirectUrl();

        return client.request(makeAuthUrl(RedsysConstants.Urls.OAUTH))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, authClientId)
                .queryParam(QueryKeys.SCOPE, authScopes)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.CODE_CHALLENGE, codeChallenge)
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.CODE_CHALLENGE_METHOD)
                .getUrl();
    }

    public OAuth2Token getToken(String code, String codeVerifier) {
        final String redirectUri = getRedirectUrl();

        final String payload =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.AUTHORIZATION_CODE)
                        .put(FormKeys.CLIENT_ID, authClientId)
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

        final String payload =
                Form.builder()
                        .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                        .put(FormKeys.ASPSP, aspsp)
                        .put(FormKeys.CLIENT_ID, authClientId)
                        .put(FormKeys.REFRESH_TOKEN, refreshToken)
                        .build()
                        .serialize();

        return client.request(url)
                .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    private Map<String, Object> getTppRedirectHeaders(String state) {
        final URL redirectUrl = new URL(getRedirectUrl()).queryParam(QueryKeys.STATE, state);
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
                        signingKeyId, eidasProxyConfiguration, eidasIdentity, allHeaders);
        allHeaders.put(HeaderKeys.SIGNATURE, signature);
        allHeaders.put(HeaderKeys.TPP_SIGNATURE_CERTIFICATE, clientSigningCertificate);

        RequestBuilder builder =
                client.request(url)
                        .addBearerToken(token)
                        .headers(allHeaders)
                        .accept(MediaType.APPLICATION_JSON);

        if (payload != null) {
            builder = builder.body(serializedPayload, MediaType.APPLICATION_JSON);
        }

        return builder;
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
        headers.put(HeaderKeys.CONSENT_ID, consentId);

        return createSignedRequest(makeApiUrl(Urls.BALANCES, accountId), null, headers)
                .get(AccountBalancesResponse.class);
    }

    private LocalDate transactionsFromDate(String accountId) {
        if (hasDoneInitialFetch(accountId) || isAutoRefresh()) {
            return LocalDate.now().minusDays(RedsysConstants.DEFAULT_REFRESH_DAYS);
        } else {
            // This might trigger SCA
            return aspspConfiguration.oldestTransactionDate();
        }
    }

    private boolean isAutoRefresh() {
        return !request.isManual();
    }

    private boolean hasDoneInitialFetch(String accountId) {
        return persistentStorage.containsKey(StorageKeys.FETCHED_TRANSACTIONS + accountId);
    }

    private void markFetchedAccount(String accountId) {
        // When an account is marked as fetched,
        final String key = StorageKeys.FETCHED_TRANSACTIONS + accountId;
        persistentStorage.put(key, "true");
    }

    private BaseTransactionsResponse<? extends TransactionEntity> fetchTransactions(
            RequestBuilder builder) {
        final HttpResponse response = builder.get(HttpResponse.class);
        final BaseTransactionsResponse<? extends TransactionEntity> transactionsResponse =
                response.getBody(aspspConfiguration.getTransactionsResponseClass());
        // Add Request ID from response header
        final String requestId = response.getHeaders().getFirst(HeaderKeys.REQUEST_ID);
        transactionsResponse.setRequestId(requestId);

        return transactionsResponse;
    }

    private BaseTransactionsResponse<? extends TransactionEntity> fetchTransactions(
            String accountId, String consentId, LocalDate fromDate, LocalDate toDate) {
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);

        final RequestBuilder builder =
                createSignedRequest(makeApiUrl(Urls.TRANSACTIONS, accountId), null, headers)
                        .queryParam(QueryKeys.DATE_FROM, formatter.format(fromDate))
                        .queryParam(QueryKeys.DATE_TO, formatter.format(toDate))
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BookingStatus.BOOKED);
        final BaseTransactionsResponse<? extends TransactionEntity> response =
                fetchTransactions(builder);
        markFetchedAccount(accountId);
        return response;
    }

    public BaseTransactionsResponse<? extends TransactionEntity> fetchPendingTransactions(
            String accountId, String consentId) {
        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);

        final RequestBuilder builder =
                createSignedRequest(makeApiUrl(Urls.TRANSACTIONS, accountId), null, headers)
                        .queryParam(QueryKeys.BOOKING_STATUS, BookingStatus.PENDING);
        return fetchTransactions(builder);
    }

    public BaseTransactionsResponse<? extends TransactionEntity> fetchTransactions(
            String accountId, String consentId, @Nullable PaginationKey key) {
        if (Objects.isNull(key)) {
            // Initial transactions request
            final LocalDate toDate = LocalDate.now();
            final LocalDate fromDate = transactionsFromDate(accountId);
            return fetchTransactions(accountId, consentId, fromDate, toDate);
        }

        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);
        headers.put(HeaderKeys.REQUEST_ID, key.getRequestId());

        final RequestBuilder builder =
                createSignedRequest(makeApiUrl(key.getPath()), null, headers);
        return fetchTransactions(builder);
    }

    public void setPsuIpAddress(String psuIpAddress) {
        this.psuIpAddress = psuIpAddress;
    }
}
