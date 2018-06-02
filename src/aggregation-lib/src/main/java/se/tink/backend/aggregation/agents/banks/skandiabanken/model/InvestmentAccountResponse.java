package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentAccountResponse {
    private String disposableAmount;
    private String id;
    private String marketValue;
    private List<PortfolioEntity> portfolios;

    public String getDisposableAmount() {
        return disposableAmount;
    }

    public void setDisposableAmount(String disposableAmount) {
        this.disposableAmount = disposableAmount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public List<PortfolioEntity> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(
            List<PortfolioEntity> portfolios) {
        this.portfolios = portfolios;
    }
}
