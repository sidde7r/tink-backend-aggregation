package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardInvoiceInfo {
    private AmountEntity balanceInvoiced;
    private AmountEntity balanceNotInvoiced;
    private AmountEntity credit;
    private AmountEntity spendable;
    private AmountEntity usedCredit;

    public AmountEntity getBalanceInvoiced() {
        return balanceInvoiced;
    }

    public void setBalanceInvoiced(AmountEntity balanceInvoiced) {
        this.balanceInvoiced = balanceInvoiced;
    }

    public AmountEntity getBalanceNotInvoiced() {
        return balanceNotInvoiced;
    }

    public void setBalanceNotInvoiced(AmountEntity balanceNotInvoiced) {
        this.balanceNotInvoiced = balanceNotInvoiced;
    }

    public AmountEntity getCredit() {
        return credit;
    }

    public void setCredit(AmountEntity credit) {
        this.credit = credit;
    }

    public AmountEntity getSpendable() {
        return spendable;
    }

    public void setSpendable(AmountEntity spendable) {
        this.spendable = spendable;
    }

    public AmountEntity getUsedCredit() {
        return usedCredit;
    }

    public void setUsedCredit(AmountEntity usedCredit) {
        this.usedCredit = usedCredit;
    }
}
