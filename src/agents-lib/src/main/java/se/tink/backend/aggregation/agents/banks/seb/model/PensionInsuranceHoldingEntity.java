package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PensionInsuranceHoldingEntity {
    @JsonProperty("FORS_NR")
    private String insuranceNumber;
    @JsonProperty("FOND_NR")
    private String fundNumber;
    @JsonProperty("FOND_NAMN_SMA")
    private String name;
    @JsonProperty("FOND_KURS_BELOPP")
    private Double marketValue;
    @JsonProperty("ANDEL_ANTAL")
    private Double quantity;
    @JsonProperty("ANDEL_BELOPP")
    private Double totalMarketValue;
    @JsonProperty("RORL_KOSTN_FORS")
    private Double variableCost;
    @JsonProperty("FAST_KOSTN_FORS")
    private Double fixedCost;
    @JsonProperty("GAV")
    private Double averageAcquisitionCost;
    @JsonProperty("ANSKAFF_KOSTNAD")
    private Double acquisitionCost;

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getFundNumber() {
        return fundNumber;
    }

    public void setFundNumber(String fundNumber) {
        this.fundNumber = fundNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Double marketValue) {
        this.marketValue = marketValue;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getTotalMarketValue() {
        return totalMarketValue;
    }

    public void setTotalMarketValue(Double totalMarketValue) {
        this.totalMarketValue = totalMarketValue;
    }

    public Double getVariableCost() {
        return variableCost;
    }

    public void setVariableCost(Double variableCost) {
        this.variableCost = variableCost;
    }

    public Double getFixedCost() {
        return fixedCost;
    }

    public void setFixedCost(Double fixedCost) {
        this.fixedCost = fixedCost;
    }

    public Double getAverageAcquisitionCost() {
        return averageAcquisitionCost;
    }

    public void setAverageAcquisitionCost(Double averageAcquisitionCost) {
        this.averageAcquisitionCost = averageAcquisitionCost;
    }

    public Double getAcquisitionCost() {
        return acquisitionCost;
    }

    public void setAcquisitionCost(Double acquisitionCost) {
        this.acquisitionCost = acquisitionCost;
    }
}
