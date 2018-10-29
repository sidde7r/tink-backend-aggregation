package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.transactionalaccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

import java.util.Collection;
import java.util.Optional;

public class AsLhvTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final AsLhvApiClient apiClient;
    private static final Logger logger = LoggerFactory.getLogger(AsLhvApiClient.class);

    public AsLhvTransactionalAccountFetcher(final AsLhvApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        Optional<Integer> baseCurrencyId = apiClient.getSessionStorage().getBaseCurrencyId();
        if (!baseCurrencyId.isPresent()) {
            throw new IllegalStateException("Base currency was not set during session.");
        }

        Optional<String> currency = apiClient.getSessionStorage().getCurrency(baseCurrencyId.get());
        if (!currency.isPresent()) {
            throw new IllegalStateException("Base currency could not be mapped during session.");
        }

        Optional<String> currentUser = apiClient.getSessionStorage().getCurrentUser();
        if (!currentUser.isPresent()) {
            throw new IllegalStateException("Current user was not set during session.");
        }

        GetUserDataResponse response = apiClient.getUserData();
        if (!response.requestSuccessful()) {
            throw new IllegalStateException(String.format("User data request failed: %s", response.getError()));
        }

        return response.getAccounts(currentUser.get(), currency.get(), baseCurrencyId.get());
    }
}