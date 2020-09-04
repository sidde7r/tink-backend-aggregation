package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfoliosEntity {

    private List<PortfolioEntity> portfolio;

    public List<PortfolioEntity> getPortfolio() {
        return portfolio;
    }
}
