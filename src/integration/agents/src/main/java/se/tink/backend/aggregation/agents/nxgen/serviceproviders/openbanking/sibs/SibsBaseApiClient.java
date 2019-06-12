package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.base.Preconditions;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.PathParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsRequest.SibsRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SibsBaseApiClient {

    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected SibsConfiguration configuration;

    public SibsBaseApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    protected void setConfiguration(SibsConfiguration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);
    }

    protected SibsRequestBuilder createRequest(URL url) {
        return SibsRequest.builder(client, configuration, url);
    }

    private String getConsentFromStorage() {
        return persistentStorage
                .get(StorageKeys.CONSENT_ID, String.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public AccountsResponse fetchAccounts() {
        URL accounts = createUrl(SibsConstants.Urls.ACCOUNTS);
        return createRequest(
                        (accounts.parameter(
                                PathParameterKeys.ASPSP_CDE, configuration.getAspspCode())))
                .signed()
                .inSession(getConsentFromStorage())
                .build()
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public BalancesResponse getAccountBalances(String accountId) {
        URL accountBalances = createUrl(SibsConstants.Urls.ACCOUNT_BALANCES);
        return createRequest(
                        accountBalances
                                .parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode())
                                .parameter(PathParameterKeys.ACCOUNT_ID, accountId))
                .signed()
                .inSession(getConsentFromStorage())
                .build()
                .queryParam(QueryKeys.PSU_INVOLVED, String.valueOf(true))
                .get(BalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getAccountTransactions(
            TransactionalAccount account) {
        URL accountTransactions = createUrl(SibsConstants.Urls.ACCOUNT_TRANSACTIONS);
        SimpleDateFormat formatter = new SimpleDateFormat(Formats.PAGINATION_DATE_FORMAT);
        return createRequest(
                        accountTransactions
                                .parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode())
                                .parameter(
                                        PathParameterKeys.ACCOUNT_ID, account.getApiIdentifier()))
                .signed()
                .inSession(getConsentFromStorage())
                .build()
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(QueryKeys.PSU_INVOLVED, String.valueOf(true))
                .queryParam(QueryKeys.BOOKING_STATUS, SibsConstants.QueryValues.BOTH)
                .queryParam(QueryKeys.DATE_FROM, formatter.format(new Date(0)))
                .get(TransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForKey(String key) {
        String baseUrl = configuration.getBaseUrl();
        return createRequest(new URL(baseUrl + key))
                .signed()
                .inSession(getConsentFromStorage())
                .build()
                .queryParam(QueryKeys.PSU_INVOLVED, String.valueOf(true))
                .get(TransactionsResponse.class);
    }

    public URL buildAuthorizeUrl(String state) {
        ConsentRequest consentRequest = getConsentRequest();
        String digest = SibsUtils.getDigest(consentRequest);
        URL createConsent = createUrl(SibsConstants.Urls.CREATE_CONSENT);
        ConsentResponse consentResponse =
                createRequest(
                                createConsent.parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode()))
                        .signed(digest)
                        .build()
                        .header(
                                HeaderKeys.TPP_REDIRECT_URI,
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
        String digest = SibsUtils.getDigest(consentRequest);
        URL createConsent = createUrl(SibsConstants.Urls.CREATE_CONSENT);
        ConsentResponse consentResponse =
                createRequest(
                                createConsent.parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode()))
                        .signed(digest)
                        .build()
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
        URL consentStatus = createUrl(SibsConstants.Urls.CONSENT_STATUS);
        return createRequest(
                        consentStatus
                                .parameter(
                                        PathParameterKeys.ASPSP_CDE, configuration.getAspspCode())
                                .parameter(PathParameterKeys.CONSENT_ID, getConsentFromStorage()))
                .signed()
                .inSession(getConsentFromStorage())
                .build()
                .get(ConsentStatusResponse.class);
    }

    private ConsentRequest getConsentRequest() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, 1); // Consent valid for 1 day

        SimpleDateFormat formatter = new SimpleDateFormat(Formats.CONSENT_BODY_DATE_FORMAT);

        return new ConsentRequest(
                new ConsentAccessEntity(SibsConstants.FormValues.ALL_ACCOUNTS),
                false,
                formatter.format(c.getTime()),
                SibsConstants.FormValues.FREQUENCY_PER_DAY,
                false);
    }

    private URL createUrl(String path) {
        String baseUrl = configuration.getBaseUrl();
        return new URL(baseUrl + path);
    }
}
