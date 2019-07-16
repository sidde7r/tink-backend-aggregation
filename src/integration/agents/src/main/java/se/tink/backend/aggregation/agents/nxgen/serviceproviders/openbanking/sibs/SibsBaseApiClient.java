package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.base.Preconditions;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsBaseApiClient {

    private static final DateTimeFormatter CONSENT_BODY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(Formats.CONSENT_BODY_DATE_FORMAT);
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected SibsConfiguration configuration;
    protected EidasProxyConfiguration eidasConf;

    public SibsBaseApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    protected void setConfiguration(
            SibsConfiguration configuration, EidasProxyConfiguration eidasConf) {
        this.configuration = Preconditions.checkNotNull(configuration);
        this.eidasConf = Preconditions.checkNotNull(eidasConf);
    }

    private String getConsentFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public AccountsResponse fetchAccounts() {
        URL accounts = createUrl(SibsConstants.Urls.ACCOUNTS);
        AccountsResponse response =
                client.request(accounts)
                        .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                        .header(HeaderKeys.CONSENT_ID, getConsentFromStorage())
                        .get(AccountsResponse.class);

        return response;
    }

    public BalancesResponse getAccountBalances(String accountId) {
        URL accountBalances =
                createUrl(SibsConstants.Urls.ACCOUNT_BALANCES)
                        .parameter(PathParameterKeys.ACCOUNT_ID, accountId);

        return client.request(accountBalances)
                .queryParam(QueryKeys.PSU_INVOLVED, String.valueOf(true))
                .header(HeaderKeys.CONSENT_ID, getConsentFromStorage())
                .get(BalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getAccountTransactions(
            TransactionalAccount account) {
        URL accountTransactions =
                createUrl(SibsConstants.Urls.ACCOUNT_TRANSACTIONS)
                        .parameter(PathParameterKeys.ACCOUNT_ID, account.getApiIdentifier());

        SimpleDateFormat formatter = new SimpleDateFormat(Formats.PAGINATION_DATE_FORMAT);

        return client.request(accountTransactions)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(QueryKeys.PSU_INVOLVED, String.valueOf(true))
                .queryParam(QueryKeys.BOOKING_STATUS, SibsConstants.QueryValues.BOTH)
                .queryParam(QueryKeys.DATE_FROM, formatter.format(new Date(0)))
                .header(HeaderKeys.CONSENT_ID, getConsentFromStorage())
                .get(TransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForKey(String key) {
        String baseUrl = configuration.getBaseUrl();

        return client.request(new URL(baseUrl + key))
                .queryParam(QueryKeys.PSU_INVOLVED, String.valueOf(true))
                .header(HeaderKeys.CONSENT_ID, getConsentFromStorage())
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
                                        .queryParam(QueryKeys.STATE, state)
                                        .queryParam(QueryKeys.CODE, SibsUtils.getRequestId()))
                        .post(ConsentResponse.class, consentRequest);

        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return new URL(consentResponse.getLinks().getRedirect());
    }

    public ConsentResponse createDecoupledAuthConsent(
            String state, String psuIdType, String psuId) {
        ConsentRequest consentRequest = getConsentRequest();

        URL createConsent = createUrl(SibsConstants.Urls.CREATE_CONSENT);
        ConsentResponse consentResponse =
                client.request(createConsent)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
                                new URL(configuration.getRedirectUrl())
                                        .queryParam(QueryKeys.STATE, state)
                                        .queryParam(QueryKeys.CODE, SibsUtils.getRequestId()))
                        .header(SibsConstants.HeaderKeys.PSU_ID_TYPE, psuIdType)
                        .header(SibsConstants.HeaderKeys.PSU_ID, psuId)
                        .post(ConsentResponse.class, consentRequest);

        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        return consentResponse;
    }

    public ConsentStatusResponse getConsentStatus() {
        URL consentStatus =
                createUrl(SibsConstants.Urls.CONSENT_STATUS)
                        .parameter(PathParameterKeys.CONSENT_ID, getConsentFromStorage());
        return client.request(consentStatus).get(ConsentStatusResponse.class);
    }

    private ConsentRequest getConsentRequest() {
        String validOneDay = getOneDayValidConsentStringDate();
        return new ConsentRequest(
                new ConsentAccessEntity(SibsConstants.FormValues.ALL_ACCOUNTS),
                true,
                validOneDay,
                SibsConstants.FormValues.FREQUENCY_PER_DAY,
                false);
    }

    private String getOneDayValidConsentStringDate() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayLater = now.plusDays(1);
        return CONSENT_BODY_DATE_FORMATTER.format(oneDayLater);
    }

    private URL createUrl(String path) {
        String baseUrl = configuration.getBaseUrl();
        return new URL(baseUrl + path)
                .parameter(PathParameterKeys.ASPSP_CDE, configuration.getAspspCode());
    }
}
