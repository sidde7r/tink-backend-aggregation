package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.fetcher.AsLhvAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class AsLhvCreditCardAccountFetcher extends AsLhvAccountFetcher
        implements AccountFetcher<CreditCardAccount> {
    private final AsLhvSessionStorage sessionStorage;

    public AsLhvCreditCardAccountFetcher(
            final AsLhvApiClient apiClient, final AsLhvSessionStorage sessionStorage) {
        super(apiClient);
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        final GetUserDataResponse userData = fetchUserData();
        return userData.getCreditCardAccounts(
                sessionStorage.getCurrentUser(),
                sessionStorage.getCurrency(sessionStorage.getBaseCurrencyId()),
                sessionStorage.getBaseCurrencyId());
    }
}
