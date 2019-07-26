package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;

public interface WithPortfoliosStep<T> {

    InvestmentBalanceStep<T> addPortfolios(List<PortfolioModule> portfolioModules);

    InvestmentBalanceStep<T> addPortfolios(PortfolioModule... portfolioModules);

    /** Used when the investment account has no holdings. */
    InvestmentBalanceStep<T> withoutPortfolios();
}
