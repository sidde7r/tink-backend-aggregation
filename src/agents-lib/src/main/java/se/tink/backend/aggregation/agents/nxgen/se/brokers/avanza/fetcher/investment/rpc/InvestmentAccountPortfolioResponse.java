package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentAccountPortfolioResponse extends PortfolioEntity {
    public PortfolioEntity getPortfolio() {
        return this;
    }
}
