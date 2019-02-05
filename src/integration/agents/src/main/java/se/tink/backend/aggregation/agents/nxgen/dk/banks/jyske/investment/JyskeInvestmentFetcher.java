package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

public class JyskeInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final JyskeApiClient apiClient;

    public JyskeInvestmentFetcher(JyskeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return this.apiClient.fetchInvestment().toInvestmentAccounts();
    }
}
