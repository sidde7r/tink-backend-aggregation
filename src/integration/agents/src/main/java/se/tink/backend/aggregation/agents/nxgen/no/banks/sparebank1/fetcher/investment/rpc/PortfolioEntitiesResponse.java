package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Slf4j
public class PortfolioEntitiesResponse {
    private List<PortfolioEntity> portfolios;

    public List<PortfolioEntity> getPortfolios() {
        return portfolios == null ? Collections.emptyList() : portfolios;
    }
}
