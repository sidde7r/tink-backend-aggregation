package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class AsLhvTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AsLhvApiClient apiClient;

    public AsLhvTransactionalAccountFetcher(final AsLhvApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AsLhvSessionStorage sessionStorage = apiClient.getSessionStorage();
        final GetUserDataResponse userData = sessionStorage.getUserData();
        return userData.getTransactionalAccounts(
                sessionStorage.getCurrentUser(),
                sessionStorage.getCurrency(sessionStorage.getBaseCurrencyId()),
                sessionStorage.getBaseCurrencyId());
    }
}
