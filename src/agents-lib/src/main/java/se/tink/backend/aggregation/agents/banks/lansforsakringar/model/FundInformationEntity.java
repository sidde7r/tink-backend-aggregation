package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundInformationEntity {
    private Long fundId;
    private String name;
    private String company;
    private String isinCode;
    private Double fee;
    private Double minBuyAmount;
    private Double minMonthlySavingsAmount;
    private Double minFirstTimeBuyAmount;
    private Double minFirstTimeMonthlySavingsAmount;
    private Integer risk;
    private String riskText;
    private Boolean recommended;
    private Boolean internal;
    private String factSheetURI;
    private String type;
    private Long valuationDate;
    private Boolean complex;
    private Integer mostSoldRanking;

    public Long getFundId() {
        return fundId;
    }

    public void setFundId(Long fundId) {
        this.fundId = fundId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public void setIsinCode(String isinCode) {
        this.isinCode = isinCode;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Double getMinBuyAmount() {
        return minBuyAmount;
    }

    public void setMinBuyAmount(Double minBuyAmount) {
        this.minBuyAmount = minBuyAmount;
    }

    public Double getMinMonthlySavingsAmount() {
        return minMonthlySavingsAmount;
    }

    public void setMinMonthlySavingsAmount(Double minMonthlySavingsAmount) {
        this.minMonthlySavingsAmount = minMonthlySavingsAmount;
    }

    public Double getMinFirstTimeBuyAmount() {
        return minFirstTimeBuyAmount;
    }

    public void setMinFirstTimeBuyAmount(Double minFirstTimeBuyAmount) {
        this.minFirstTimeBuyAmount = minFirstTimeBuyAmount;
    }

    public Double getMinFirstTimeMonthlySavingsAmount() {
        return minFirstTimeMonthlySavingsAmount;
    }

    public void setMinFirstTimeMonthlySavingsAmount(Double minFirstTimeMonthlySavingsAmount) {
        this.minFirstTimeMonthlySavingsAmount = minFirstTimeMonthlySavingsAmount;
    }

    public Integer getRisk() {
        return risk;
    }

    public void setRisk(Integer risk) {
        this.risk = risk;
    }

    public String getRiskText() {
        return riskText;
    }

    public void setRiskText(String riskText) {
        this.riskText = riskText;
    }

    public Boolean getRecommended() {
        return recommended;
    }

    public void setRecommended(Boolean recommended) {
        this.recommended = recommended;
    }

    public Boolean getInternal() {
        return internal;
    }

    public void setInternal(Boolean internal) {
        this.internal = internal;
    }

    public String getFactSheetURI() {
        return factSheetURI;
    }

    public void setFactSheetURI(String factSheetURI) {
        this.factSheetURI = factSheetURI;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(Long valuationDate) {
        this.valuationDate = valuationDate;
    }

    public Boolean getComplex() {
        return complex;
    }

    public void setComplex(Boolean complex) {
        this.complex = complex;
    }

    public Integer getMostSoldRanking() {
        return mostSoldRanking;
    }

    public void setMostSoldRanking(Integer mostSoldRanking) {
        this.mostSoldRanking = mostSoldRanking;
    }
}
