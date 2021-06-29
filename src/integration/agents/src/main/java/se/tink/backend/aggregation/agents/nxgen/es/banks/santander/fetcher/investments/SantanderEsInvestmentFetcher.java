package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class SantanderEsInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final FundsAccountsFetcher fundsAccountsFetcher;
    private final PortfolioAccountsFetcher portfolioAccountsFetcher;

    public SantanderEsInvestmentFetcher(
            FundsAccountsFetcher fundsAccountsFetcher, PortfolioAccountsFetcher portfolioAccountsFetcher) {
        this.fundsAccountsFetcher = fundsAccountsFetcher;
        this.portfolioAccountsFetcher = portfolioAccountsFetcher;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        Collection<InvestmentAccount> investmentAccounts = portfolioAccountsFetcher.fetchAccounts();
        investmentAccounts.addAll(fundsAccountsFetcher.fetchAccounts());
        return investmentAccounts;
    }
}
