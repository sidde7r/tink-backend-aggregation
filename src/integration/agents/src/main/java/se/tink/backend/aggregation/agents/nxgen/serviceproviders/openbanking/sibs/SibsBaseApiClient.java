package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.base.Preconditions;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.QueryKeys;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SibsBaseApiClient {

    private static final AggregationLogger log = new AggregationLogger(SibsBaseApiClient.class);
    private static final DateTimeFormatter CONSENT_BODY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Formats.CONSENT_BODY_DATE_FORMAT);
    private static final String TRUE = "true";
    private final String isPsuInvolved;
    protected final TinkHttpClient client;
    protected SibsConfiguration configuration;
    protected EidasProxyConfiguration eidasConf;
    private final SibsUserState userState;

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
            TinkHttpClient client, SibsUserState userState, boolean isRequestManual) {
        this.client = client;
        this.userState = userState;
        this.isPsuInvolved = String.valueOf(isRequestManual);
    }

    protected void setConfiguration(
            SibsConfiguration configuration, EidasProxyConfiguration eidasConf) {
        this.configuration = Preconditions.checkNotNull(configuration);
        this.eidasConf = Preconditions.checkNotNull(eidasConf);
    }

    public AccountsResponse fetchAccounts() {
        URL accounts = createUrl(SibsConstants.Urls.ACCOUNTS);
        return client.request(accounts)
                .queryParam(QueryKeys.WITH_BALANCE, TRUE)
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
                .get(AccountsResponse.class);
    }

    public BalancesResponse getAccountBalances(String accountId) {
        URL accountBalances =
                createUrl(SibsConstants.Urls.ACCOUNT_BALANCES)
                        .parameter(PathParameterKeys.ACCOUNT_ID, accountId);

        return client.request(accountBalances)
                .queryParam(QueryKeys.PSU_INVOLVED, isPsuInvolved)
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
                .get(BalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getAccountTransactions(
            TransactionalAccount account) {
        URL accountTransactions =
                createUrl(SibsConstants.Urls.ACCOUNT_TRANSACTIONS)
                        .parameter(PathParameterKeys.ACCOUNT_ID, account.getApiIdentifier());
        String transactionFetchFromDate = userState.getTransactionsFetchBeginDate(account);
        log.info("Fetching transactions starting from " + transactionFetchFromDate);
        return client.request(accountTransactions)
                .queryParam(QueryKeys.WITH_BALANCE, TRUE)
                .queryParam(QueryKeys.PSU_INVOLVED, isPsuInvolved)
                .queryParam(QueryKeys.BOOKING_STATUS, SibsConstants.QueryValues.BOTH)
                .queryParam(QueryKeys.DATE_FROM, transactionFetchFromDate)
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
                .get(TransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForKey(String key) {
        String baseUrl = configuration.getBaseUrl();

        return client.request(new URL(baseUrl + key))
                .queryParam(QueryKeys.PSU_INVOLVED, isPsuInvolved)
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
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

        userState.startManualAuthentication(consentResponse);

        return new URL(consentResponse.getLinks().getRedirect());
    }

    public ConsentStatusResponse getConsentStatus() throws SessionException {
        try {
            URL consentStatus =
                    createUrl(SibsConstants.Urls.CONSENT_STATUS)
                            .parameter(PathParameterKeys.CONSENT_ID, userState.getConsentId());
            return client.request(consentStatus).get(ConsentStatusResponse.class);
        } catch (IllegalStateException ex) {
            if (ex.getCause() instanceof SessionException) {
                throw (SessionException) ex.getCause();
            }
            throw ex;
        }
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
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
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
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
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
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .put(SibsPaymentUpdateResponse.class, sibsPaymentUpdateRequest);
    }

    public SibsPaymentUpdateResponse updatePaymentforPsuId(
            String updatePsuIdUrl, SibsPaymentUpdateRequest sibsPaymentUpdateRequest) {

        URL updatePaymentUrl = createUrl(updatePsuIdUrl);

        return client.request(updatePaymentUrl)
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
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
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
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
                .header(HeaderKeys.CONSENT_ID, userState.getConsentId())
                .get(SibsGetPaymentStatusResponse.class);
    }
}
