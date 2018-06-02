package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FundHoldingDetailEntity extends AbstractResponse {
    private boolean availableGuardian;
    private AmountEntity averageValueOfCost;
    private String account;
    private String fundBelongingType;
    private int holdingId;
    private double holdingUnits;
    private String isin;
    private String marketValueFormatted;
    private String marketValueCurrency;
    private boolean openForPurchase;
    private boolean openForSell;
    private AmountEntity price;
    private AmountEntity purchaseValue;
    private AmountEntity totalChange;

    public boolean isAvailableGuardian() {
        return availableGuardian;
    }

    public void setAvailableGuardian(boolean availableGuardian) {
        this.availableGuardian = availableGuardian;
    }

    public AmountEntity getAverageValueOfCost() {
        return averageValueOfCost;
    }

    public void setAverageValueOfCost(
            AmountEntity averageValueOfCost) {
        this.averageValueOfCost = averageValueOfCost;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getFundBelongingType() {
        return fundBelongingType;
    }

    public void setFundBelongingType(String fundBelongingType) {
        this.fundBelongingType = fundBelongingType;
    }

    public int getHoldingId() {
        return holdingId;
    }

    public void setHoldingId(int holdingId) {
        this.holdingId = holdingId;
    }

    public double getHoldingUnits() {
        return holdingUnits;
    }

    public void setHoldingUnits(double holdingUnits) {
        this.holdingUnits = holdingUnits;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getMarketValueFormatted() {
        return marketValueFormatted;
    }

    public void setMarketValueFormatted(String marketValueFormatted) {
        this.marketValueFormatted = marketValueFormatted;
    }

    public String getMarketValueCurrency() {
        return marketValueCurrency;
    }

    public void setMarketValueCurrency(String marketValueCurrency) {
        this.marketValueCurrency = marketValueCurrency;
    }

    public boolean isOpenForPurchase() {
        return openForPurchase;
    }

    public void setOpenForPurchase(boolean openForPurchase) {
        this.openForPurchase = openForPurchase;
    }

    public boolean isOpenForSell() {
        return openForSell;
    }

    public void setOpenForSell(boolean openForSell) {
        this.openForSell = openForSell;
    }

    public AmountEntity getPrice() {
        return price;
    }

    public void setPrice(AmountEntity price) {
        this.price = price;
    }

    public AmountEntity getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(AmountEntity purchaseValue) {
        this.purchaseValue = purchaseValue;
    }

    public AmountEntity getTotalChange() {
        return totalChange;
    }

    public void setTotalChange(AmountEntity totalChange) {
        this.totalChange = totalChange;
    }
}
