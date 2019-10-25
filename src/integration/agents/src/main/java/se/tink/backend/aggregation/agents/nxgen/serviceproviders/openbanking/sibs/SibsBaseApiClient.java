package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.base.Preconditions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.SibsSignSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsCancelPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsGetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsGetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentInitiationRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentInitiationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentUpdateRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc.SibsPaymentUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.Consent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsBaseApiClient {

    private static final DateTimeFormatter CONSENT_BODY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Formats.CONSENT_BODY_DATE_FORMAT);
    private static final String TRUE = "true";
    private final String isPsuInvolved;
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected SibsConfiguration configuration;
    protected EidasProxyConfiguration eidasConf;

    /*
    * TODO: remove this section after full AIS and PIS test:
    // String requestTimestamp = new SimpleDateFormat(Formats.CONSENT_BODY_DATE_FORMAT, Locale.ENGLISH).format(new Date());
    // .header(HeaderKeys.DATE, requestTimestamp)
    * It was kept because sibs date header problems.
    * Sibs might use different header patterns for different endpoints (even in same service)
    * If Invalid Header shows verify if it's single or global call problem for:
     * - global - change pattern in sign interceptor
     * - single - use code above to create date with correct pattern and add header (it won't be override)
    */

    public SibsBaseApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, boolean isRequestManual) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.isPsuInvolved = String.valueOf(isRequestManual);
    }

    protected void setConfiguration(
            SibsConfiguration configuration, EidasProxyConfiguration eidasConf) {
        this.configuration = Preconditions.checkNotNull(configuration);
        this.eidasConf = Preconditions.checkNotNull(eidasConf);
    }

    private String getConsentIdFromStorage() {
        Consent consent = getConsentFromStorage();
        return consent.getConsentId();
    }

    private Consent getConsentFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, Consent.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public AccountsResponse fetchAccounts() {
        URL accounts = createUrl(SibsConstants.Urls.ACCOUNTS);
        return client.request(accounts)
                .queryParam(QueryKeys.WITH_BALANCE, TRUE)
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .get(AccountsResponse.class);
    }

    public BalancesResponse getAccountBalances(String accountId) {
        URL accountBalances =
                createUrl(SibsConstants.Urls.ACCOUNT_BALANCES)
                        .parameter(PathParameterKeys.ACCOUNT_ID, accountId);

        return client.request(accountBalances)
                .queryParam(QueryKeys.PSU_INVOLVED, isPsuInvolved)
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .get(BalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getAccountTransactions(
            TransactionalAccount account) {
        URL accountTransactions =
                createUrl(SibsConstants.Urls.ACCOUNT_TRANSACTIONS)
                        .parameter(PathParameterKeys.ACCOUNT_ID, account.getApiIdentifier());

        return client.request(accountTransactions)
                .queryParam(QueryKeys.WITH_BALANCE, TRUE)
                .queryParam(QueryKeys.PSU_INVOLVED, isPsuInvolved)
                .queryParam(QueryKeys.BOOKING_STATUS, SibsConstants.QueryValues.BOTH)
                .queryParam(
                        QueryKeys.DATE_FROM, SibsUtils.getPaginationDate(getConsentFromStorage()))
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .get(TransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForKey(String key) {
        String baseUrl = configuration.getBaseUrl();

        return client.request(new URL(baseUrl + key))
                .queryParam(QueryKeys.PSU_INVOLVED, isPsuInvolved)
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .get(TransactionsResponse.class);
    }

    public URL buildAuthorizeUrl(String state) {
        ConsentRequest consentRequest = getConsentRequest();

        URL createConsent = createUrl(SibsConstants.Urls.CREATE_CONSENT);

        ConsentResponse consentResponse =
                client.request(createConsent)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .header(
                                SibsConstants.HeaderKeys.TPP_REDIRECT_URI,
                                new URL(configuration.getRedirectUrl())
                                        .queryParam(QueryKeys.STATE, state))
                        .post(ConsentResponse.class, consentRequest);

        saveConsentInPersistentStorage(consentResponse);

        return new URL(consentResponse.getLinks().getRedirect());
    }

    public void removeConsentFromPersistentStorage() {
        persistentStorage.remove(StorageKeys.CONSENT_ID);
    }

    private void saveConsentInPersistentStorage(ConsentResponse consentResponse) {
        Consent consent =
                new Consent(consentResponse.getConsentId(), LocalDateTime.now().toString());
        persistentStorage.put(SibsSignSteps.SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, true);
        persistentStorage.put(StorageKeys.CONSENT_ID, consent);
    }

    public ConsentStatusResponse getConsentStatus() throws SessionException {
        try {
            URL consentStatus =
                    createUrl(SibsConstants.Urls.CONSENT_STATUS)
                            .parameter(PathParameterKeys.CONSENT_ID, getConsentIdFromStorage());
            return client.request(consentStatus).get(ConsentStatusResponse.class);
        } catch (IllegalStateException ex) {
            if (ex.getCause() instanceof SessionException) {
                throw (SessionException) ex.getCause();
            }
            throw ex;
        }
    }

    public boolean isManualAuthenticationInProgress() {
        return persistentStorage
                .get(SibsSignSteps.SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, Boolean.class)
                .orElse(false);
    }

    public void markManualAuthenticationFinished() {
        persistentStorage.put(SibsSignSteps.SIBS_MANUAL_AUTHENTICATION_IN_PROGRESS, false);
    }

    private ConsentRequest getConsentRequest() {
        String valid90Days = get90DaysValidConsentStringDate();
        return new ConsentRequest(
                new ConsentAccessEntity(SibsConstants.FormValues.ALL_ACCOUNTS),
                true,
                valid90Days,
                SibsConstants.FormValues.FREQUENCY_PER_DAY,
                false);
    }

    private String get90DaysValidConsentStringDate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime days90Later = now.plusDays(90);
        return CONSENT_BODY_DATE_FORMATTER.format(days90Later);
    }

    private URL createUrl(String path) {
        String baseUrl = configuration.getBaseUrl();
        return new URL(baseUrl + path)
                .parameter(PathParameterKeys.ASPSP_CDE, configuration.getAspspCode());
    }

    public SibsPaymentInitiationResponse createPayment(
            SibsPaymentInitiationRequest sibsPaymentRequest,
            SibsPaymentType sibsPaymentType,
            String state) {
        URL createPaymentUrl = createUrl(SibsConstants.Urls.PAYMENT_INITIATION);

        return client.request(
                        createPaymentUrl.parameter(
                                PathParameterKeys.PAYMENT_PRODUCT, sibsPaymentType.getValue()))
                .header(SibsConstants.HeaderKeys.PSU_IP_ADDRESS, "127.0.0.1")
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(configuration.getRedirectUrl()).queryParam(QueryKeys.STATE, state))
                .queryParam(SibsConstants.QueryKeys.TIP_REDIRECT_PREFERRED, TRUE)
                .post(SibsPaymentInitiationResponse.class, sibsPaymentRequest);
    }

    public SibsGetPaymentResponse getPayment(String uniqueId, SibsPaymentType sibsPaymentType) {

        URL getPayment = createUrl(SibsConstants.Urls.GET_PAYMENT_REQUEST);
        return client.request(
                        getPayment
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        sibsPaymentType.getValue())
                                .parameter(PathParameterKeys.PAYMENT_ID, uniqueId))
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .get(SibsGetPaymentResponse.class);
    }

    public SibsPaymentUpdateResponse updatePayment(
            String uniqueId,
            SibsPaymentType sibsPaymentType,
            SibsPaymentUpdateRequest sibsPaymentUpdateRequest) {

        URL updatePaymentUrl = createUrl(SibsConstants.Urls.UPDATE_PAYMENT_REQUEST);

        return client.request(
                        updatePaymentUrl
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        sibsPaymentType.getValue())
                                .parameter(PathParameterKeys.PAYMENT_ID, uniqueId))
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .put(SibsPaymentUpdateResponse.class, sibsPaymentUpdateRequest);
    }

    public SibsPaymentUpdateResponse updatePaymentforPsuId(
            String updatePsuIdUrl, SibsPaymentUpdateRequest sibsPaymentUpdateRequest) {

        URL updatePaymentUrl = createUrl(updatePsuIdUrl);

        return client.request(updatePaymentUrl)
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .put(SibsPaymentUpdateResponse.class, sibsPaymentUpdateRequest);
    }

    public SibsCancelPaymentResponse cancelPayment(
            String uniqueId, SibsPaymentType sibsPaymentType) {
        URL cancelPaymentUrl = createUrl(SibsConstants.Urls.DELETE_PAYMENT_REQUEST);

        return client.request(
                        cancelPaymentUrl
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        sibsPaymentType.getValue())
                                .parameter(PathParameterKeys.PAYMENT_ID, uniqueId))
                .header(SibsConstants.HeaderKeys.PSU_IP_ADDRESS, "127.0.0.1")
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .delete(SibsCancelPaymentResponse.class);
    }

    public SibsGetPaymentStatusResponse getPaymentStatus(
            String uniqueId, SibsPaymentType sibsPaymentType) {
        URL paymentStatusUrl = createUrl(SibsConstants.Urls.GET_PAYMENT_STATUS_REQUEST);

        return client.request(
                        paymentStatusUrl
                                .parameter(
                                        PathParameterKeys.PAYMENT_PRODUCT,
                                        sibsPaymentType.getValue())
                                .parameter(PathParameterKeys.PAYMENT_ID, uniqueId))
                .header(HeaderKeys.CONSENT_ID, getConsentIdFromStorage())
                .get(SibsGetPaymentStatusResponse.class);
    }
}
