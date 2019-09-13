package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank;

import java.util.Date;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DeutscheBankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final DeutscheBankConfiguration configuration;

    public DeutscheBankApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            DeutscheBankConfiguration configuration) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
    }

    public DeutscheBankConfiguration getConfiguration() {
        return this.configuration;
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        String consentId = sessionStorage.get(StorageKeys.CONSENT_ID);
        String uuid = UUID.randomUUID().toString();

        return createRequest(url)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .header(HeaderKeys.X_REQUEST_ID, uuid);
    }

    public ConsentBaseResponse getConsent(String state, String iban, String psuId) {
        ConsentBaseRequest consentBaseRequest = new ConsentBaseRequest(iban);
        return client.request(new URL(configuration.getBaseUrl().concat(Urls.CONSENT)))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, configuration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .header(HeaderKeys.PSU_IP_ADDRESS, configuration.getPsuIpAddress())
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(configuration.getRedirectUrl()).queryParam(QueryKeys.STATE, state))
                .header(
                        HeaderKeys.TPP_NOK_REDIRECT_URI,
                        new URL(configuration.getRedirectUrl()).queryParam(QueryKeys.STATE, state))
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentBaseResponse.class, consentBaseRequest);
    }

    public ConsentStatusResponse getConsentStatus(String consentStatusLink) {
        return createRequest(new URL(consentStatusLink))
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_IP_ADDRESS, getConfiguration().getPsuIpAddress())
                .get(ConsentStatusResponse.class);
    }

    public FetchAccountsResponse fetchAccounts() {
        return createRequestInSession(new URL(configuration.getBaseUrl().concat(Urls.ACCOUNTS)))
                .get(FetchAccountsResponse.class);
    }

    public FetchBalancesResponse fetchBalances(AccountEntity accountEntity) {
        return createRequestInSession(
                        new URL(
                                configuration
                                        .getBaseUrl()
                                        .concat(
                                                String.format(
                                                        Urls.BALANCES,
                                                        accountEntity.getResourceId()))))
                .get(FetchBalancesResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> fetchTransactionsForAccount(
            TransactionalAccount account, Date fromDate, Date toDate) {

        return createRequestInSession(
                        new URL(
                                configuration
                                        .getBaseUrl()
                                        .concat(
                                                String.format(
                                                        Urls.TRANSACTIONS,
                                                        account.getApiIdentifier()))))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKING_STATUS)
                .queryParam(QueryKeys.DELTA_LIST, QueryValues.DELTA_LIST)
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }
}
