package se.tink.backend.aggregation.agents.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Instrument {

    // Normally the uniqueIdentifier should be isin + market.
    // If isin and market is hard to get hold of and the bank / broker have some other way to
    // identify the instrument
    // we can use that.
    private String uniqueIdentifier;
    private String
            isin; // An International Securities Identification Number (ISIN) uniquely identifies a
    // security.
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

    @JsonIgnore
    private static Double getAmountValueFromExactAmount(ExactCurrencyAmount amount) {
        return Optional.ofNullable(amount).map(ExactCurrencyAmount::getDoubleValue).orElse(null);
    }

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

    @Deprecated
    public void setAverageAcquisitionPrice(Double averageAcquisitionPrice) {
        this.averageAcquisitionPrice = averageAcquisitionPrice;
    }

    public void setAverageAcquisitionPrice(BigDecimal averageAcquisitionPrice) {
        this.averageAcquisitionPrice = averageAcquisitionPrice.doubleValue();
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

    @JsonIgnore
    public void setAverageAcquisitionPriceFromAmount(ExactCurrencyAmount averageAcquisitionPrice) {
        this.averageAcquisitionPrice = getAmountValueFromExactAmount(averageAcquisitionPrice);
    }

    @JsonIgnore
    public void setMarketValueFromAmount(ExactCurrencyAmount marketValue) {
        this.marketValue = getAmountValueFromExactAmount(marketValue);
    }

    @JsonIgnore
    public void setPriceFromAmount(ExactCurrencyAmount price) {
        this.price = getAmountValueFromExactAmount(price);
    }

    @JsonIgnore
    public void setProfitFromAmount(ExactCurrencyAmount profit) {
        this.profit = getAmountValueFromExactAmount(profit);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uniqueIdentifier", uniqueIdentifier)
                .add("isin", isin)
                .add("marketPlace", marketPlace)
                .add("averageAcquisitionPrice", averageAcquisitionPrice)
                .add("currency", currency)
                .add("marketValue", marketValue == null ? null : "***")
                .add("name", name)
                .add("price", price == null ? null : "***")
                .add("quantity", quantity)
                .add("profit", profit == null ? null : "***")
                .add("ticker", ticker)
                .add("type", type)
                .add("rawType", rawType)
                .toString();
    }

    public enum Type {
        FUND,
        STOCK,
        OTHER
    }
}
