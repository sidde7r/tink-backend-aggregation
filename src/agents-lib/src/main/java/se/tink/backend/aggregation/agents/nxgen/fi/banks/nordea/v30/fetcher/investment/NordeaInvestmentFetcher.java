package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

public class NordeaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final NordeaFiApiClient client;

    public NordeaInvestmentFetcher(
            NordeaFiApiClient client) {
        this.client = client;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return client.fetchInvestments().toTinkInvestmentAccounts();
    }
}
