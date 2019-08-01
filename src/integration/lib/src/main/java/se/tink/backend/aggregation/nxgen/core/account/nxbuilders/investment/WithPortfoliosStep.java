package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment;

import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;

public interface WithPortfoliosStep<T> {

    InvestmentBalanceStep<T> withPortfolios(List<PortfolioModule> portfolioModules);

    InvestmentBalanceStep<T> withPortfolios(PortfolioModule... portfolioModules);

    /** Used when the investment account has no holdings. */
    InvestmentBalanceStep<T> withoutPortfolios();
}
