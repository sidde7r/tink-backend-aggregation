package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.ParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.ParameterValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.authenticator.rpc.DirectLoginTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.configuration.OpenBankProjectConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class OpenBankProjectApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private OpenBankProjectConfiguration configuration;

    public OpenBankProjectApiClient(
            final TinkHttpClient client, final SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    private RequestBuilder createRequestInSession(final URL url) {
        final String directLoginHeaderValue =
                String.format(HeaderKeys.DIRECT_LOGIN, sessionStorage.get(HeaderKeys.TOKEN));

        return createRequest(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, directLoginHeaderValue);
    }

    private URL getBaseUrl() {
        return new URL(configuration.getBaseUrl());
    }

    public FetchAccountsResponse fetchAccounts() {
        final URL fetchAccountsUrl =
                getBaseUrl()
                        .concat(Urls.ACCOUNTS)
                        .parameter(ParameterKeys.BANK_ID, configuration.getBankId());

        return createRequestInSession(fetchAccountsUrl).get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            final TransactionalAccount account, final int page) {

        final URL fetchTransactionsUrl =
                getBaseUrl()
                        .concat(Urls.TRANSACTIONS)
                        .parameter(
                                ParameterKeys.BANK_ID,
                                account.getFromTemporaryStorage(ParameterKeys.BANK_ID))
                        .parameter(
                                ParameterKeys.ACCOUNT_ID,
                                account.getFromTemporaryStorage(ParameterKeys.ACCOUNT_ID))
                        .parameter(ParameterKeys.VIEW_ID, ParameterValues.OWNER_VIEW_ID);

        return createRequestInSession(fetchTransactionsUrl)
                .queryParam(QueryKeys.SORT_DIRECTION, QueryValues.DESC)
                .queryParam(QueryKeys.OFFSET, Integer.toString(page))
                .get(FetchTransactionsResponse.class);
    }

    public FetchAccountResponse fetchAccount(final AccountEntity accountEntity) {
        final URL fetchAccountUrl =
                getBaseUrl()
                        .concat(Urls.ACCOUNT)
                        .parameter(StorageKeys.BANK_ID, accountEntity.getBankId())
                        .parameter(StorageKeys.ACCOUNT_ID, accountEntity.getId());

        return createRequestInSession(fetchAccountUrl).get(FetchAccountResponse.class);
    }

    private RequestBuilder createRequest(final URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public DirectLoginTokenResponse getToken(final String username, final String password) {
        final String headerValue =
                String.format(
                        HeaderValues.DIRECT_LOGIN, username, password, configuration.getClientId());

        return createRequest(getBaseUrl().concat(Urls.DIRECT_LOGIN_URL))
                .header(HeaderKeys.AUTHORIZATION, headerValue)
                .post(DirectLoginTokenResponse.class);
    }

    public void setConfiguration(OpenBankProjectConfiguration openBankProjectConfiguration) {
        this.configuration = openBankProjectConfiguration;
    }
}
