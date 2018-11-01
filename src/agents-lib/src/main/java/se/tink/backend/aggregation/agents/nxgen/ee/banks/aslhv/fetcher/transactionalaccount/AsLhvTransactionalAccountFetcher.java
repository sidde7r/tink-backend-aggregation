package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class AsLhvTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AsLhvSessionStorage sessionStorage;
    public AsLhvTransactionalAccountFetcher(final AsLhvSessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final GetUserDataResponse userData = sessionStorage.getUserData();
        return userData.getTransactionalAccounts(
                sessionStorage.getCurrentUser(),
                sessionStorage.getCurrency(sessionStorage.getBaseCurrencyId()),
                sessionStorage.getBaseCurrencyId());
    }
}
