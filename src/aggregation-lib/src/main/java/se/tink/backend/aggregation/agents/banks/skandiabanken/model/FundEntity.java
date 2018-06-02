package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundEntity {
    private String id;
    private String currency;
    private String name;
    private String isin;
    private String netAssetDate;
    private String netAssetValue;
    private String netAssetValueChange;
    private String sharpe;
    private String managementFee;
    private String buyFee;
    private String standardDeviation;
    private Integer fundSpace;
    private Double netAssetValueRaw;
    private Integer rating;
    private Integer risk;
    private Boolean recommended;
    private Boolean blockedForTrade;
    private String morningstarPerformanceId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getNetAssetDate() {
        return netAssetDate;
    }

    public void setNetAssetDate(String netAssetDate) {
        this.netAssetDate = netAssetDate;
    }

    public String getNetAssetValue() {
        return netAssetValue;
    }

    public void setNetAssetValue(String netAssetValue) {
        this.netAssetValue = netAssetValue;
    }

    public String getNetAssetValueChange() {
        return netAssetValueChange;
    }

    public void setNetAssetValueChange(String netAssetValueChange) {
        this.netAssetValueChange = netAssetValueChange;
    }

    public String getSharpe() {
        return sharpe;
    }

    public void setSharpe(String sharpe) {
        this.sharpe = sharpe;
    }

    public String getManagementFee() {
        return managementFee;
    }

    public void setManagementFee(String managementFee) {
        this.managementFee = managementFee;
    }

    public String getBuyFee() {
        return buyFee;
    }

    public void setBuyFee(String buyFee) {
        this.buyFee = buyFee;
    }

    public String getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(String standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public Integer getFundSpace() {
        return fundSpace;
    }

    public void setFundSpace(Integer fundSpace) {
        this.fundSpace = fundSpace;
    }

    public Double getNetAssetValueRaw() {
        return netAssetValueRaw;
    }

    public void setNetAssetValueRaw(Double netAssetValueRaw) {
        this.netAssetValueRaw = netAssetValueRaw;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getRisk() {
        return risk;
    }

    public void setRisk(Integer risk) {
        this.risk = risk;
    }

    public Boolean getRecommended() {
        return recommended;
    }

    public void setRecommended(Boolean recommended) {
        this.recommended = recommended;
    }

    public Boolean getBlockedForTrade() {
        return blockedForTrade;
    }

    public void setBlockedForTrade(Boolean blockedForTrade) {
        this.blockedForTrade = blockedForTrade;
    }

    public String getMorningstarPerformanceId() {
        return morningstarPerformanceId;
    }

    public void setMorningstarPerformanceId(String morningstarPerformanceId) {
        this.morningstarPerformanceId = morningstarPerformanceId;
    }
}
