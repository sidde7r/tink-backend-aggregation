package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CashBalanceEntity {
    private String moneyAvailableForPurchase;
    private String moneyAvailableForWithdrawal;
    private String balance;

    public String getMoneyAvailableForPurchase() {
        return moneyAvailableForPurchase;
    }

    public void setMoneyAvailableForPurchase(String moneyAvailableForPurchase) {
        this.moneyAvailableForPurchase = moneyAvailableForPurchase;
    }

    public String getMoneyAvailableForWithdrawal() {
        return moneyAvailableForWithdrawal;
    }

    public void setMoneyAvailableForWithdrawal(String moneyAvailableForWithdrawal) {
        this.moneyAvailableForWithdrawal = moneyAvailableForWithdrawal;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}
