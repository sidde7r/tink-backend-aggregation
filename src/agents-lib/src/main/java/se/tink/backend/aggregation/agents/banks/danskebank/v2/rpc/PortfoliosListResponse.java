package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfoliosListResponse extends AbstractResponse {
    @JsonProperty("Portfolios")
    private List<PortfolioEntity> portfolios;
    @JsonProperty("TotalPortfolio")
    private TotalPortfolioEntity totalPortfolio;

    public List<PortfolioEntity> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(
            List<PortfolioEntity> portfolios) {
        this.portfolios = portfolios;
    }

    public TotalPortfolioEntity getTotalPortfolio() {
        return totalPortfolio;
    }

    public void setTotalPortfolio(TotalPortfolioEntity totalPortfolio) {
        this.totalPortfolio = totalPortfolio;
    }
}
