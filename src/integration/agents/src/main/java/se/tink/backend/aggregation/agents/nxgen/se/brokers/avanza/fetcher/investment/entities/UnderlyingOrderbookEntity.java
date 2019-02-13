package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnderlyingOrderbookEntity {
    private String currency;
    private String name;
    private String id;
    private String type;
    private double highestPrice;
    private double lowestPrice;
    private double lastPrice;
    private String lastPriceUpdated;
    private double change;
    private double changePercent;
    private String updated;
    private long totalVolumeTraded;
    private String tickerSymbol;

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public String getLastPriceUpdated() {
        return lastPriceUpdated;
    }

    public double getChange() {
        return change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public String getUpdated() {
        return updated;
    }

    public long getTotalVolumeTraded() {
        return totalVolumeTraded;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }
}
