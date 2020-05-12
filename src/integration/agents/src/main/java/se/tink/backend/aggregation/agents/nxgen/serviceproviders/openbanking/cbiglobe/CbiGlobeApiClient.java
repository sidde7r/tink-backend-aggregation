package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentPsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CbiGlobeApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private CbiGlobeConfiguration configuration;
    private boolean requestManual;
    protected TemporaryStorage temporaryStorage;
    protected InstrumentType instrumentType;

    public CbiGlobeApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            boolean requestManual,
            TemporaryStorage temporaryStorage,
            InstrumentType instrumentType) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.requestManual = requestManual;
        this.temporaryStorage = temporaryStorage;
        this.instrumentType = instrumentType;
    }

    protected CbiGlobeConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(CbiGlobeConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url).type(MediaType.APPLICATION_JSON);
    }

    protected RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url)
                .addBearerToken(authToken)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.ASPSP_CODE, configuration.getAspspCode())
                .header(HeaderKeys.DATE, CbiGlobeUtils.formatDate(new Date()));
    }

    protected RequestBuilder createRequestWithConsent(URL url) {
        RequestBuilder rb =
                createRequestInSession(url)
                        .header(
                                HeaderKeys.CONSENT_ID,
                                persistentStorage.get(StorageKeys.CONSENT_ID));
        if (requestManual) {
            rb.header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.DEFAULT_PSU_IP_ADDRESS);
        }

        return rb;
    }

    protected RequestBuilder createAccountsRequestWithConsent() {
        return createRequestInSession(getAccountsUrl())
                .header(HeaderKeys.CONSENT_ID, persistentStorage.get(StorageKeys.CONSENT_ID));
    }

    private URL getAccountsUrl() {
        return this.instrumentType.equals(InstrumentType.ACCOUNTS)
                ? Urls.ACCOUNTS
                : Urls.CARD_ACCOUNTS;
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        MessageCodes.NO_ACCESS_TOKEN_IN_STORAGE.name()));
    }

    public GetTokenResponse getToken(String clientId, String clientSecret) {
        return createRequest(Urls.TOKEN)
                .addBasicAuth(clientId, clientSecret)
                .queryParam(QueryKeys.GRANT_TYPE, QueryValues.CLIENT_CREDENTIALS)
                .queryParam(QueryKeys.SCOPE, QueryValues.PRODUCTION)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(GetTokenResponse.class);
    }

    public void getAndSaveToken() {
        GetTokenResponse getTokenResponse =
                getToken(configuration.getClientId(), configuration.getClientSecret());
        OAuth2Token token = getTokenResponse.toTinkToken();
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
    }

    public ConsentResponse createConsent(
            String state, ConsentType consentType, ConsentRequest consentRequest) {
        RequestBuilder request = createConsentRequest(state, consentType);
        return request.post(ConsentResponse.class, consentRequest);
    }

    protected RequestBuilder createConsentRequest(String state, ConsentType consentType) {
        String redirectUrl = createRedirectUrl(state, consentType);
        return createRequestInSession(Urls.CONSENTS)
                .header(HeaderKeys.ASPSP_PRODUCT_CODE, configuration.getAspspProductCode())
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, redirectUrl);
    }

    public String createRedirectUrl(String state, ConsentType consentType) {
        return new URL(configuration.getRedirectUrl())
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.CODE, consentType.getCode())
                .get();
    }

    public ConsentResponse updateConsent(String consentId, UpdateConsentRequest body) {
        return createRequestInSession(Urls.CONSENTS.concat("/" + consentId))
                .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA)
                .put(ConsentResponse.class, body);
    }

    public ConsentResponse updateConsentPsuCredentials(
            String consentId, UpdateConsentPsuCredentialsRequest body) {
        return createRequestInSession(Urls.CONSENTS.concat("/" + consentId))
                .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA)
                .put(ConsentResponse.class, body);
    }

    public GetAccountsResponse getAccounts() {
        return createAccountsRequestWithConsent().get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String resourceId) {
        return createRequestWithConsent(getBalancesUrl().parameter(IdTags.ACCOUNT_ID, resourceId))
                .get(GetBalancesResponse.class);
    }

    private URL getBalancesUrl() {
        return this.instrumentType.equals(InstrumentType.ACCOUNTS)
                ? Urls.BALANCES
                : Urls.CARD_BALANCES;
    }

    public GetTransactionsResponse getTransactions(
            String apiIdentifier,
            LocalDate fromDate,
            LocalDate toDate,
            String bookingType,
            int page) {
        HttpResponse response =
                createRequestWithConsent(
                                getTransactionsUrl().parameter(IdTags.ACCOUNT_ID, apiIdentifier))
                        .queryParam(QueryKeys.BOOKING_STATUS, bookingType)
                        .queryParam(QueryKeys.DATE_FROM, fromDate.toString())
                        .queryParam(QueryKeys.DATE_TO, toDate.toString())
                        .queryParam(QueryKeys.OFFSET, String.valueOf(page))
                        .get(HttpResponse.class);

        String totalPages = getTotalPages(response, apiIdentifier);

        temporaryStorage.putIfAbsent(apiIdentifier, totalPages);

        GetTransactionsResponse getTransactionsResponse =
                response.getBody(GetTransactionsResponse.class);

        if (Objects.nonNull(totalPages) && Integer.valueOf(totalPages) > page) {
            getTransactionsResponse.setPageRemaining(true);
        }
        return getTransactionsResponse;
    }

    private URL getTransactionsUrl() {
        return this.instrumentType.equals(InstrumentType.ACCOUNTS)
                ? Urls.TRANSACTIONS
                : Urls.CARD_TRANSACTIONS;
    }

    private String getTotalPages(HttpResponse response, String apiIdentifier) {
        return Optional.ofNullable(response.getHeaders().getFirst(QueryKeys.TOTAL_PAGES))
                .orElse(temporaryStorage.get(apiIdentifier));
    }

    public boolean isTokenValid() {
        return getTokenFromStorage().isValid();
    }

    public ConsentStatus getConsentStatus(String consentType) throws SessionException {
        return createRequestInSession(
                        Urls.CONSENTS_STATUS.parameter(
                                IdTags.CONSENT_ID, getConsentIdFromStorage(consentType)))
                .get(ConsentResponse.class)
                .getConsentStatus();
    }

    private String getConsentIdFromStorage(String consentType) throws SessionException {
        return persistentStorage
                .get(consentType, String.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest) {

        return createRequestInSession(Urls.PAYMENT)
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.DEFAULT_PSU_IP_ADDRESS)
                .header(HeaderKeys.ASPSP_PRODUCT_CODE, configuration.getAspspProductCode())
                .header(HeaderKeys.TPP_REDIRECT_PREFERRED, "true")
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(getConfiguration().getRedirectUrl())
                                .queryParam(QueryKeys.STATE, sessionStorage.get(QueryKeys.STATE))
                                .queryParam(HeaderKeys.CODE, HeaderValues.CODE))
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public CreatePaymentResponse getPayment(String uniqueId) {
        return createRequestInSession(Urls.FETCH_PAYMENT.parameter(IdTags.PAYMENT_ID, uniqueId))
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.DEFAULT_PSU_IP_ADDRESS)
                .header(HeaderKeys.ASPSP_PRODUCT_CODE, configuration.getAspspProductCode())
                .get(CreatePaymentResponse.class);
    }

    public CreatePaymentResponse getPaymentStatus(String uniqueId) {
        return createRequestInSession(
                        Urls.FETCH_PAYMENT_STATUS.parameter(IdTags.PAYMENT_ID, uniqueId))
                .get(CreatePaymentResponse.class);
    }

    public GetAccountsResponse fetchAccounts() {
        GetAccountsResponse getAccountsResponse =
                SerializationUtils.deserializeFromString(
                        persistentStorage.get(StorageKeys.ACCOUNTS), GetAccountsResponse.class);

        if (getAccountsResponse == null) {
            getAccountsResponse = getAccounts();
            persistentStorage.put(StorageKeys.ACCOUNTS, getAccountsResponse);
        }
        return getAccountsResponse;
    }
}
