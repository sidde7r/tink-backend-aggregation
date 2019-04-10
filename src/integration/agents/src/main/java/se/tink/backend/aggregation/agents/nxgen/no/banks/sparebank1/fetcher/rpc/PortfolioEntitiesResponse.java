package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.PortfolioEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioEntitiesResponse {
    private List<PortfolioEntity> portfolios;
    private Boolean hasNomineePortfolios;
    private Boolean hasVPSPortfolios;
    private Boolean hasASKPortfolio;

    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    public List<PortfolioEntity> getPortfolios() {
        return portfolios == null ? Collections.emptyList() : portfolios;
    }

    public Boolean getHasNomineePortfolios() {
        return hasNomineePortfolios;
    }

    public Boolean getHasVPSPortfolios() {
        return hasVPSPortfolios;
    }

    public Boolean getHasASKPortfolio() {
        return hasASKPortfolio;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}
