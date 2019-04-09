package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalPortfolioEntity {
    @JsonProperty("ChangeValue")
    private Double changeValue;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("TotalValue")
    private Double totalValue;

    public Double getChangeValue() {
        return changeValue;
    }

    public void setChangeValue(Double changeValue) {
        this.changeValue = changeValue;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }
}
