package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTradeListId() {
        return tradeListId;
    }

    public void setTradeListId(String tradeListId) {
        this.tradeListId = tradeListId;
    }

    public String getChangeOfPrice() {
        return changeOfPrice;
    }

    public void setChangeOfPrice(String changeOfPrice) {
        this.changeOfPrice = changeOfPrice;
    }

    public String getChangeOfPriceInPercentage() {
        return changeOfPriceInPercentage;
    }

    public void setChangeOfPriceInPercentage(String changeOfPriceInPercentage) {
        this.changeOfPriceInPercentage = changeOfPriceInPercentage;
    }

    public String getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(String buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public String getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(String sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(String latestPrice) {
        this.latestPrice = latestPrice;
    }

    public String getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(String highestPrice) {
        this.highestPrice = highestPrice;
    }

    public String getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(String lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public String getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(String totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getLastTradePriceUpdateTime() {
        return lastTradePriceUpdateTime;
    }

    public void setLastTradePriceUpdateTime(String lastTradePriceUpdateTime) {
        this.lastTradePriceUpdateTime = lastTradePriceUpdateTime;
    }

    public Double getPriceCalculationFactor() {
        return priceCalculationFactor;
    }

    public void setPriceCalculationFactor(Double priceCalculationFactor) {
        this.priceCalculationFactor = priceCalculationFactor;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public Boolean getWatched() {
        return watched;
    }

    public void setWatched(Boolean watched) {
        this.watched = watched;
    }
}
