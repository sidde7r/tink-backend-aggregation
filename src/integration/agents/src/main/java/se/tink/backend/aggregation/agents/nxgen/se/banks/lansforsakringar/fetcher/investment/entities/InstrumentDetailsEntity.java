package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentDetailsEntity {
    private String currency;
    private String name;
    private String symbol;
    private String tradeListId;
    private String changeOfPrice;
    private String changeOfPriceInPercentage;
    private String buyingPrice;
    private String sellingPrice;
    private String latestPrice;
    private String highestPrice;
    private String lowestPrice;
    private String totalVolume;
    private String lastTradePriceUpdateTime;
    private Double priceCalculationFactor;
    private String isin;
    private Boolean watched;

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getTradeListId() {
        return tradeListId;
    }

    public String getChangeOfPrice() {
        return changeOfPrice;
    }

    public String getChangeOfPriceInPercentage() {
        return changeOfPriceInPercentage;
    }

    public String getBuyingPrice() {
        return buyingPrice;
    }

    public String getSellingPrice() {
        return sellingPrice;
    }

    public String getLatestPrice() {
        return latestPrice;
    }

    public String getHighestPrice() {
        return highestPrice;
    }

    public String getLowestPrice() {
        return lowestPrice;
    }

    public String getTotalVolume() {
        return totalVolume;
    }

    public String getLastTradePriceUpdateTime() {
        return lastTradePriceUpdateTime;
    }

    public Double getPriceCalculationFactor() {
        return priceCalculationFactor;
    }

    public String getIsin() {
        return isin;
    }

    public Boolean getWatched() {
        return watched;
    }
}
