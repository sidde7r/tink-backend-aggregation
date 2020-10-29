package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NordeaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final NordeaBaseApiClient apiClient;
    private final NordeaConfiguration nordeaConfiguration;

    public NordeaTransactionalAccountFetcher(
            NordeaBaseApiClient apiClient, NordeaConfiguration nordeaConfiguration) {
        this.apiClient = apiClient;
        this.nordeaConfiguration = nordeaConfiguration;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccount().toTinkAccount(nordeaConfiguration);
    }
}
