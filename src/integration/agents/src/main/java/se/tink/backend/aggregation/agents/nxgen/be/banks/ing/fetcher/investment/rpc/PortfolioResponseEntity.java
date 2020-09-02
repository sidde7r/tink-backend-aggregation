package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.entities.PortfoliosEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioResponseEntity extends BaseMobileResponseEntity {
    private PortfoliosEntity portfolios;

    public PortfoliosEntity getPortfolios() {
        return portfolios;
    }
}
