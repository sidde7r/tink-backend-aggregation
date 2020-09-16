package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NordeaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final NordeaSEApiClient apiClient;

    public NordeaTransactionalAccountFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccount().toTinkAccount();
    }
}
