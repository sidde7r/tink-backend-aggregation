package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.HoldingsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HoldingsResponse {
    private List<HoldingsEntity> portfolioHoldings;
    private Boolean periodicReportsIncluded;

    public List<HoldingsEntity> getPortfolioHoldings() {
        return portfolioHoldings;
    }

    public Boolean getPeriodicReportsIncluded() {
        return periodicReportsIncluded;
    }
}
