package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.configuration.DeutscheBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntityBerlinGroup;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.TransactionsKeyPaginatorBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;

public final class DeutscheBankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private DeutscheBankConfiguration configuration;

    public DeutscheBankApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private DeutscheBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(DeutscheBankConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
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

    public ConsentBaseResponse getConsent(String state, String iban) {
        ConsentBaseRequest consentBaseRequest = new ConsentBaseRequest();
        consentBaseRequest.setAccess(new AccessEntityBerlinGroup());
        consentBaseRequest.getAccess().addIban(iban);

        return client.request(getConfiguration().getBaseUrl() + Urls.CONSENT)
                .body(consentBaseRequest.toData())
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.PSU_ID_TYPE, HeaderValues.PSU_ID_TYPE)
                .header(HeaderKeys.PSU_ID, HeaderValues.PSU_ID)
                .header(HeaderKeys.PSY_IP_ADDRESS, HeaderValues.PSY_IP_ADDRESS)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        new URL(getConfiguration().getRedirectUrl())
                                .queryParam(QueryKeys.STATE, state))
                .type(MediaType.APPLICATION_JSON)
                .post(ConsentBaseResponse.class);
    }

    public FetchAccountsResponse fetchAccounts() {
        FetchAccountsResponse fetchAccountsResponse =
                createRequestInSession(new URL(getConfiguration().getBaseUrl() + Urls.ACCOUNTS))
                        .queryParam(QueryKeys.WITH_BALANCE, QueryValues.WITH_BALANCE)
                        .get(FetchAccountsResponse.class);

        // DB withBalances=true doesn't actually work so we have to fetch them manually.
        fetchAccountsResponse.getAccounts().forEach(this::fetchBalanceForAccount);

        return fetchAccountsResponse;
    }

    private void fetchBalanceForAccount(AccountEntity account) {
        FetchBalancesResponse res =
                createRequestInSession(
                                new URL(
                                        getConfiguration().getBaseUrl()
                                                + String.format(
                                                        Urls.BALANCES, account.getResourceId())))
                        .get(FetchBalancesResponse.class);

        account.setBalances(res.getAccount().get(0).getBalances());
    }

    public TransactionKeyPaginatorResponse<String> fetchTransactionsForAccount(
            TransactionalAccount account, Date fromDate, Date toDate) {

        return createRequestInSession(
                        new URL(
                                getConfiguration().getBaseUrl()
                                        + String.format(
                                                Urls.TRANSACTIONS, account.getApiIdentifier())))
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKING_STATUS)
                .queryParam(QueryKeys.DELTA_LIST, QueryValues.DELTA_LIST)
                .get(TransactionsKeyPaginatorBaseResponse.class);
    }
}
