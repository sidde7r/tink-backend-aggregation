package se.tink.backend.aggregation.agents.models;

import java.util.Optional;

public class Instrument {

    public enum Type {
        FUND, STOCK, OTHER
    }

    // Normally the uniqueIdentifier should be isin + market.
    // If isin and market is hard to get hold of and the bank / broker have some other way to identify the instrument
    // we can use that.
    private String uniqueIdentifier;
    private String isin; // An International Securities Identification Number (ISIN) uniquely identifies a security.
    private String marketPlace;
    private Double averageAcquisitionPrice;
    private String currency;
    private Double marketValue;
    private String name;
    private Double price;
    private Double quantity;
    private Double profit;
    private String ticker;
    private Type type;
    private String rawType;

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getMarketPlace() {
        return marketPlace;
    }

    public void setMarketPlace(String marketPlace) {
        this.marketPlace = marketPlace;
    }

    public Double getAverageAcquisitionPrice() {
        return averageAcquisitionPrice;
    }

    public void setAverageAcquisitionPrice(Double averageAcquisitionPrice) {
        this.averageAcquisitionPrice = averageAcquisitionPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getProfit() {
        return profit;
    }

    public void setProfit(Double profit) {
        this.profit = profit;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getRawType() {
        return rawType;
    }

    public void setRawType(String rawType) {
        this.rawType = rawType;
    }

    public void setAverageAcquisitionPriceFromAmount(Amount averageAcquisitionPrice) {
        this.averageAcquisitionPrice = getAmountValue(averageAcquisitionPrice);
    }

    public void setMarketValueFromAmount(Amount marketValue) {
        this.marketValue = getAmountValue(marketValue);
    }

    public void setPriceFromAmount(Amount price) {
        this.price = getAmountValue(price);
    }

    public void setProfitFromAmount(Amount profit) {
        this.profit = getAmountValue(profit);
    }

    private static Double getAmountValue(Amount amount) {
        return Optional.ofNullable(amount)
                .map(Amount::getValue)
                .orElse(null);
    }
}
