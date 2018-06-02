package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioEntity {

    private String portfolioId;
    private double marketValue;
    private double totalProfit;

    public String getPortfolioId() {
        return portfolioId;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }
}
