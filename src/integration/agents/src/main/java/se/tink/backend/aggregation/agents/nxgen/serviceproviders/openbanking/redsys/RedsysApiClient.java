package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import no.finn.unleash.UnleashContext;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.AspspConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.AccountBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.filters.ErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.entities.RedsysPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.CancelPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.PaymentInitiationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.PaymentInitiationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.payments.src.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc.ErrorResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateBusinessScope;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants;

@Slf4j
public class RedsysApiClient {

    private static final Map<String, ConsentStatus>
            CONSENT_403_RESPONSE_ERROR_CODE_TO_CONSENT_STATUS_MAPPING = new HashMap<>();

    static {
        CONSENT_403_RESPONSE_ERROR_CODE_TO_CONSENT_STATUS_MAPPING.put(
                "CONSENT_UNKNOWN", ConsentStatus.UNKNOWN);
        CONSENT_403_RESPONSE_ERROR_CODE_TO_CONSENT_STATUS_MAPPING.put(
                "RESOURCE_UNKNOWN", ConsentStatus.REVOKED_BY_PSU);
    }

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private String redirectUrl;
    private AspspConfiguration aspspConfiguration;
    private String authClientId;
    private String authScopes;
    private ConsentStatus cachedConsentStatus = ConsentStatus.UNKNOWN;
    private final AgentComponentProvider componentProvider;
    private final RedsysSignedRequestFactory redsysSignedRequestFactory;

    RedsysApiClient(
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            AspspConfiguration aspspConfiguration,
            AgentComponentProvider componentProvider,
            RedsysSignedRequestFactory redsysSignedRequestFactory) {
        this.componentProvider = componentProvider;
        this.client = componentProvider.getTinkHttpClient();
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.aspspConfiguration = aspspConfiguration;
        this.redsysSignedRequestFactory = redsysSignedRequestFactory;

        client.addFilter(
                new ErrorFilter(
                        HttpErrorCodes.TOO_MANY_REQUESTS,
                        ErrorCodes.ACCESS_EXCEEDED,
                        BankServiceError.ACCESS_EXCEEDED));
        client.addFilter(
                new ErrorFilter(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        ErrorCodes.SERVER_ERROR,
                        BankServiceError.BANK_SIDE_FAILURE));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(
            AgentConfiguration<RedsysConfiguration> agentConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        try {
            this.authClientId =
                    CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQsealc());
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
        String authorizeEndpoint =
                getAuthorizeEndpoint(
                        componentProvider.getUnleashClient(),
                        componentProvider
                                .getCredentialsRequest()
                                .getCredentials()
                                .getProviderName());
        componentProvider
                .getContext()
                .getLogMasker()
                .addNewSensitiveValuesToMasker(Collections.singleton(codeChallenge));
        return client.request(makeAuthUrl(authorizeEndpoint))
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

        return client.request(makeAuthUrl(Urls.TOKEN))
                .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public Pair<String, URL> requestConsent(String scaState, ConsentRequestBody body) {
        final String url = makeApiUrl(Urls.CONSENTS);
        final GetConsentResponse getConsentResponse =
                redsysSignedRequestFactory
                        .createSignedRequest(url, body, getTppRedirectHeaders(scaState))
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
        try {
            final String url = makeApiUrl(Urls.CONSENT, consentId);
            return redsysSignedRequestFactory.createSignedRequest(url).get(ConsentResponse.class);
        } catch (HttpResponseException ex) {
            ErrorResponse errorResponse = ErrorResponse.fromResponse(ex.getResponse());
            if (ex.getResponse().getStatus() == 403) {
                return CONSENT_403_RESPONSE_ERROR_CODE_TO_CONSENT_STATUS_MAPPING.entrySet().stream()
                        .filter(e -> errorResponse.hasErrorCode(e.getKey()))
                        .findAny()
                        .map(e -> new ConsentResponse(e.getValue()))
                        .orElseThrow(() -> ex);
            }
            throw ex;
        }
    }

    public ConsentStatus fetchConsentStatus(String consentId) {
        // If valid, cache it for the session
        if (cachedConsentStatus == ConsentStatus.VALID) {
            return cachedConsentStatus;
        }
        final String url = makeApiUrl(Urls.CONSENT_STATUS, consentId);
        final ConsentStatusResponse consentStatusResponse =
                redsysSignedRequestFactory
                        .createSignedRequest(url)
                        .get(ConsentStatusResponse.class);
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

    public ListAccountsResponse fetchAccounts(String consentId) {
        RequestBuilder builder =
                redsysSignedRequestFactory
                        .createSignedRequest(makeApiUrl(Urls.ACCOUNTS))
                        .header(HeaderKeys.CONSENT_ID, consentId);
        if (aspspConfiguration.shouldRequestAccountsWithBalance()) {
            builder = builder.queryParam(QueryKeys.WITH_BALANCE, QueryValues.TRUE);
        }
        return builder.get(ListAccountsResponse.class);
    }

    public AccountBalancesResponse fetchAccountBalances(String accountId, String consentId) {
        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);

        return redsysSignedRequestFactory
                .createSignedRequest(makeApiUrl(Urls.BALANCES, accountId), null, headers)
                .get(AccountBalancesResponse.class);
    }

    private LocalDate transactionsFromDate(String accountId, LocalDate lastDateOfTransaction) {
        if (hasDoneInitialFetch(accountId) || isAutoRefresh()) {
            return ObjectUtils.firstNonNull(
                    lastDateOfTransaction,
                    LocalDate.now().minusDays(RedsysConstants.DEFAULT_REFRESH_DAYS));
        } else {
            // This might trigger SCA
            return aspspConfiguration.oldestTransactionDate();
        }
    }

    private boolean isAutoRefresh() {
        return !componentProvider.getCredentialsRequest().getUserAvailability().isUserPresent();
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
        HttpResponse response = null;
        try {
            response = builder.get(HttpResponse.class);
        } catch (HttpResponseException ex) {
            handleKnownErrors(ex);
        }

        final BaseTransactionsResponse<? extends TransactionEntity> transactionsResponse =
                response.getBody(aspspConfiguration.getTransactionsResponseClass());
        // Add Request ID from response header
        final String requestId = response.getHeaders().getFirst(HeaderKeys.REQUEST_ID);
        transactionsResponse.setRequestId(requestId);

        return transactionsResponse;
    }

    private void handleKnownErrors(HttpResponseException hre) {
        final ErrorResponse error = ErrorResponse.fromResponse(hre.getResponse());
        if (error.hasErrorCode(ErrorCodes.CONSENT_EXPIRED)) {
            throw SessionError.SESSION_EXPIRED.exception();
        } else if (HttpStatus.SC_BAD_REQUEST == hre.getResponse().getStatus()
                && error.hasErrorCode(ErrorCodes.SERVER_ERROR)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
        throw hre;
    }

    public BaseTransactionsResponse<? extends TransactionEntity> fetchPendingTransactions(
            String accountId, String consentId) {
        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);

        final RequestBuilder builder =
                redsysSignedRequestFactory
                        .createSignedRequest(
                                makeApiUrl(Urls.TRANSACTIONS, accountId), null, headers)
                        .queryParam(QueryKeys.BOOKING_STATUS, BookingStatus.PENDING);
        return fetchTransactions(builder);
    }

    public BaseTransactionsResponse<? extends TransactionEntity> fetchTransactionsWithKey(
            PaginationKey nextKey, String consentId) {
        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);
        headers.put(HeaderKeys.REQUEST_ID, nextKey.getRequestId());

        final RequestBuilder builder =
                redsysSignedRequestFactory.createSignedRequest(
                        makeApiUrl(nextKey.getPath()), null, headers);
        return fetchTransactions(builder);
    }

    public BaseTransactionsResponse<? extends TransactionEntity> fetchTransactions(
            String accountId, String consentId, LocalDate dateOfLastTransation) {

        final LocalDate toDate = LocalDate.now();
        final LocalDate fromDate = transactionsFromDate(accountId, dateOfLastTransation);
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);

        final RequestBuilder builder =
                redsysSignedRequestFactory
                        .createSignedRequest(
                                makeApiUrl(Urls.TRANSACTIONS, accountId), null, headers)
                        .queryParam(QueryKeys.DATE_FROM, formatter.format(fromDate))
                        .queryParam(QueryKeys.DATE_TO, formatter.format(toDate))
                        .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BookingStatus.BOOKED);
        final BaseTransactionsResponse<? extends TransactionEntity> response =
                fetchTransactions(builder);
        markFetchedAccount(accountId);
        return response;
    }

    public PaymentInitiationResponse createPayment(
            String paymentProduct, PaymentInitiationRequest requestBody, String consentId) {
        return redsysSignedRequestFactory
                .createSignedRequest(
                        makeApiUrl(Urls.CREATE_PAYMENT, paymentProduct),
                        requestBody,
                        preparePaymentInitiationHeaders(consentId))
                .body(requestBody)
                .post(PaymentInitiationResponse.class);
    }

    public PaymentStatusResponse fetchPaymentStatus(
            PaymentScheme paymentProduct, String paymentId) {
        return redsysSignedRequestFactory
                .createSignedRequest(
                        makeApiUrl(
                                Urls.FETCH_PAYMENT_STATUS,
                                RedsysPaymentType.fromTinkPaymentType(paymentProduct),
                                paymentId))
                .get(PaymentStatusResponse.class);
    }

    public CancelPaymentResponse cancelPayment(PaymentRequest paymentRequest) {
        return redsysSignedRequestFactory
                .createSignedRequest(
                        makeApiUrl(
                                Urls.CANCEL_PAYMENT,
                                paymentRequest.getPayment().getPaymentScheme(),
                                paymentRequest.getPayment().getUniqueId()))
                .delete(CancelPaymentResponse.class);
    }

    private Map<String, Object> preparePaymentInitiationHeaders(String consentId) {
        final Map<String, Object> headers = Maps.newHashMap();
        headers.put(HeaderKeys.CONSENT_ID, consentId);
        headers.putAll(getTppRedirectHeaders(sessionStorage.get(Storage.STATE)));
        return headers;
    }

    private String getAuthorizeEndpoint(UnleashClient unleashClient, String currentProviderName) {
        Toggle toggle =
                Toggle.of("redsys-app-to-app-redirect")
                        .context(
                                UnleashContext.builder()
                                        .addProperty(
                                                Constants.Context.PROVIDER_NAME.getValue(),
                                                currentProviderName)
                                        .build())
                        .build();

        if (unleashClient.isToggleEnabled(toggle)) {
            log.info("[REDSYS APP TO APP REDIRECT] Enabled.");
            return Urls.BIOMETRIC;
        } else {
            log.info("[REDSYS APP TO APP REDIRECT] Disabled.");
            return Urls.OAUTH;
        }
    }
}
