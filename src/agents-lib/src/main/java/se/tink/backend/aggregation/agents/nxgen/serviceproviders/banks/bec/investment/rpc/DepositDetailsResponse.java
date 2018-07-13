package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DepositDetailsResponse {
    private String depotid;
    private int refreshRate;
    private int warningRate;
    private int maxRefreshTime;
    @JsonProperty("papers")
    private List<PortfolioEntity> portfolios;

    public String getDepotid() {
        return depotid;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public int getWarningRate() {
        return warningRate;
    }

    public int getMaxRefreshTime() {
        return maxRefreshTime;
    }

    public List<PortfolioEntity> getPortfolios() {
        return portfolios;
    }
}
