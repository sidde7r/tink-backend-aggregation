package se.tink.backend.aggregation.agents.models;

import com.google.common.base.MoreObjects;
import java.util.List;

public class Portfolio {

    public enum Type {
        ISK,
        KF,
        DEPOT,
        PENSION,
        OTHER
    }

    // For an account with one portfolio this can be the account nr. For accounts with multiple
    // portfolios this can be
    // the account nr combined with some static name/type/id only applicable to this portfolio.
    private String uniqueIdentifier;

    // The total profit of the entire portfolio, both historical (real) profit and current
    // (potential) profit.
    private Double totalProfit;
    // The funds, on this portfolio, available for purchase instruments.
    private Double cashValue;
    private Double totalValue;
    private Type type;
    private String rawType;
    private List<Instrument> instruments;

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(Double totalProfit) {
        this.totalProfit = totalProfit;
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

    public List<Instrument> getInstruments() {
        return instruments;
    }

    public void setInstruments(List<Instrument> instruments) {
        this.instruments = instruments;
    }

    public Double getCashValue() {
        return cashValue;
    }

    public void setCashValue(Double cashValue) {
        this.cashValue = cashValue;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uniqueIdentifier", uniqueIdentifier)
                .add("totalProfit", totalProfit)
                .add("cashValue", cashValue == null ? null : "***")
                .add("totalValue", totalValue == null ? null : "***")
                .add("type", type)
                .add("rawType", rawType)
                .add("instruments", instruments)
                .toString();
    }
}
