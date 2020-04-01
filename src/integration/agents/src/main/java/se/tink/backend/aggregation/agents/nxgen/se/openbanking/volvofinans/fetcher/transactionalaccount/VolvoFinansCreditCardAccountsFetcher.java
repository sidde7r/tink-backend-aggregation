package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class VolvoFinansCreditCardAccountsFetcher implements AccountFetcher<CreditCardAccount> {
    private final VolvoFinansApiClient apiClient;

    public VolvoFinansCreditCardAccountsFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toCreditCardAccounts();
    }
}
