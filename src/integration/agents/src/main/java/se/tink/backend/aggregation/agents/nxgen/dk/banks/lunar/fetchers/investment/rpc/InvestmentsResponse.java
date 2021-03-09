package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvestmentsResponse {
    private PortfolioEntity portfolio;
}
