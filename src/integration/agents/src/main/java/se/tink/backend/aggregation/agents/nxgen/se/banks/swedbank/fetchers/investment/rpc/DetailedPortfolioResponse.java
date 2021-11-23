package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class DetailedPortfolioResponse {
    private DetailedPortfolioEntity detailedHolding;
    private String serverTime;
}
