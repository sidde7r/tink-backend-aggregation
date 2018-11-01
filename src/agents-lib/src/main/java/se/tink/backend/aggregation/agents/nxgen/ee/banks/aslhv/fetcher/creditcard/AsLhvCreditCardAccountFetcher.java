package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

public class AsLhvCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final AsLhvApiClient apiClient;

    public AsLhvCreditCardAccountFetcher(final AsLhvApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final AsLhvSessionStorage sessionStorage = apiClient.getSessionStorage();
        final GetUserDataResponse userData = sessionStorage.getUserData();
        return userData.getCreditCardAccounts(
                sessionStorage.getCurrentUser(),
                sessionStorage.getCurrency(sessionStorage.getBaseCurrencyId()),
                sessionStorage.getBaseCurrencyId());
    }
}

