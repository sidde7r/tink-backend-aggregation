package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class VolvoFinansTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {

    private final VolvoFinansApiClient apiClient;

    public VolvoFinansTransactionalAccountFetcher(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().toTinkAccounts();
    }
}
