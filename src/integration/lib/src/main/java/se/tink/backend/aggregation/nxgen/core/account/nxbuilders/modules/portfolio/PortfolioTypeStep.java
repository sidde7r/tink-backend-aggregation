package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;

public interface PortfolioTypeStep<T> {
    /** @param type Portfolio type mapped to {@link PortfolioType} */
    PortfolioIdStep<T> withType(PortfolioType type);
}
