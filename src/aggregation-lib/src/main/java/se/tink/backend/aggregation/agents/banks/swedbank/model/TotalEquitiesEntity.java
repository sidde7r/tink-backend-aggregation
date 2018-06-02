package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TotalEquitiesEntity {
    private AmountEntity value;
    private AmountEntity acquisitionValue;
    private String changePercent;
    private AmountEntity buyingPower;
    private AmountEntity marketValue;
    private AmountEntity changeAbsolute;

    public AmountEntity getValue() {
        return value;
    }

    public void setValue(AmountEntity value) {
        this.value = value;
    }

    public AmountEntity getAcquisitionValue() {
        return acquisitionValue;
    }

    public void setAcquisitionValue(AmountEntity acquisitionValue) {
        this.acquisitionValue = acquisitionValue;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public AmountEntity getBuyingPower() {
        return buyingPower;
    }

    public void setBuyingPower(AmountEntity buyingPower) {
        this.buyingPower = buyingPower;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(AmountEntity marketValue) {
        this.marketValue = marketValue;
    }

    public AmountEntity getChangeAbsolute() {
        return changeAbsolute;
    }

    public void setChangeAbsolute(AmountEntity changeAbsolute) {
        this.changeAbsolute = changeAbsolute;
    }
}
