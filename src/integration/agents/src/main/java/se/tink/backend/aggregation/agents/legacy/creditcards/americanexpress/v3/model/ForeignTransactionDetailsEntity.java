package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

public class ForeignTransactionDetailsEntity {
    private String exchangeRate;
    private String exchangeRateLabel;
    private String commission;
    private String commissionLabel;
    private String foreignAmount;
    private String foreignAmountLabel;

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getExchangeRateLabel() {
        return exchangeRateLabel;
    }

    public void setExchangeRateLabel(String exchangeRateLabel) {
        this.exchangeRateLabel = exchangeRateLabel;
    }

    public String getCommission() {
        return commission;
    }

    public void setCommission(String commission) {
        this.commission = commission;
    }

    public String getCommissionLabel() {
        return commissionLabel;
    }

    public void setCommissionLabel(String commissionLabel) {
        this.commissionLabel = commissionLabel;
    }

    public String getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(String foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public String getForeignAmountLabel() {
        return foreignAmountLabel;
    }

    public void setForeignAmountLabel(String foreignAmountLabel) {
        this.foreignAmountLabel = foreignAmountLabel;
    }
}
