package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import se.tink.libraries.pair.Pair;

public class PortfolioIsinPair extends Pair<PortfolioEntity, IsinMap> {
    public PortfolioIsinPair(PortfolioEntity portfolio, IsinMap isinMap) {
        super(portfolio, isinMap);
    }

    public PortfolioEntity getPortfolio() {
        return this.first;
    }

    public IsinMap getIsinMap() {
        return this.second;
    }
}
