package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class VolksbankCheckingAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final VolksbankApiClient apiClient;

    public VolksbankCheckingAccountFetcher(
            VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        // TODO Parse stuff in the response of post Main.
        apiClient.postMain();
        return null;
    }
}
