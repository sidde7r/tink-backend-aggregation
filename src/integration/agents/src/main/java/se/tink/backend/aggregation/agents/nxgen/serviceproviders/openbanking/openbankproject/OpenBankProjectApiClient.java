package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.authenticator.rpc.DirectLoginTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class OpenBankProjectApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public OpenBankProjectApiClient(
            final TinkHttpClient client,
            final SessionStorage sessionStorage,
            final PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createRequestInSession(final URL url) {
        final String directLoginHeaderValue =
                String.format(HeaderKeys.DIRECT_LOGIN, sessionStorage.get(HeaderKeys.TOKEN));

        return createRequest(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, directLoginHeaderValue);
    }

    private URL getBaseUrl() {
        return new URL(persistentStorage.get(OpenBankProjectConstants.StorageKeys.BASE_URL));
    }

    public FetchAccountsResponse fetchAccounts() {
        final URL fetchAccountsUrl =
                getBaseUrl()
                        .concat(OpenBankProjectConstants.Urls.ACCOUNTS)
                        .parameter(
                                OpenBankProjectConstants.ParameterKeys.BANK_ID,
                                persistentStorage.get(
                                        OpenBankProjectConstants.StorageKeys.BANK_ID));

        return createRequestInSession(fetchAccountsUrl).get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            final TransactionalAccount account, final int page) {

        final URL fetchTransactionsUrl =
                getBaseUrl()
                        .concat(OpenBankProjectConstants.Urls.TRANSACTIONS)
                        .parameter(
                                OpenBankProjectConstants.ParameterKeys.BANK_ID,
                                account.getFromTemporaryStorage(
                                        OpenBankProjectConstants.ParameterKeys.BANK_ID))
                        .parameter(
                                OpenBankProjectConstants.ParameterKeys.ACCOUNT_ID,
                                account.getFromTemporaryStorage(
                                        OpenBankProjectConstants.ParameterKeys.ACCOUNT_ID))
                        .parameter(
                                OpenBankProjectConstants.ParameterKeys.VIEW_ID,
                                OpenBankProjectConstants.ParameterValues.OWNER_VIEW_ID);

        return createRequestInSession(fetchTransactionsUrl)
                .queryParam(
                        OpenBankProjectConstants.QueryKeys.SORT_DIRECTION,
                        OpenBankProjectConstants.QueryValues.DESC)
                .queryParam(OpenBankProjectConstants.QueryKeys.OFFSET, Integer.toString(page))
                .get(FetchTransactionsResponse.class);
    }

    public FetchAccountResponse fetchAccount(final AccountEntity accountEntity) {
        final URL fetchAccountUrl =
                getBaseUrl()
                        .concat(OpenBankProjectConstants.Urls.ACCOUNT)
                        .parameter(
                                OpenBankProjectConstants.StorageKeys.BANK_ID,
                                accountEntity.getBankId())
                        .parameter(
                                OpenBankProjectConstants.StorageKeys.ACCOUNT_ID,
                                accountEntity.getId());

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
                        HeaderValues.DIRECT_LOGIN,
                        username,
                        password,
                        persistentStorage.get(OpenBankProjectConstants.StorageKeys.CLIENT_ID));

        return createRequest(getBaseUrl().concat(OpenBankProjectConstants.Urls.DIRECT_LOGIN_URL))
                .header(HeaderKeys.AUTHORIZATION, headerValue)
                .post(DirectLoginTokenResponse.class);
    }
}
