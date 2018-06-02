package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MastercardEntity {
    private String agreementAccountRegNo;
    private String agreementAccountAccountNo;
    private String agreementName;
    private String agreementAccountOwner;
    private Double balanceTotal;
    private String balanceCurrency;
    private Double availableTotal;
    private String availableTotalCurrency;
    private Double maxBalanceTotal;
    private String maxBalanceTotalCurrency;
    private Double nextBillingTotal;
    private String billingForm;
    private String billingText;
    private String cardNo;
    private String cardUser;
    private String cardName;
    private Double balance;
    private Double maxBalance;
    private Double nextBilling;
    private Double available;
    private String cardStatus;
    private boolean stopped;
    private String latestUpdate;
    private String billingAccountRegNo;
    private String billingAccountAccountNo;
    private String cardUserCprNo;

    public String getAgreementAccountRegNo() {
        return agreementAccountRegNo;
    }

    public String getAgreementAccountAccountNo() {
        return agreementAccountAccountNo;
    }

    public String getAgreementName() {
        return agreementName;
    }

    public String getAgreementAccountOwner() {
        return agreementAccountOwner;
    }

    public Double getBalanceTotal() {
        return balanceTotal;
    }

    public String getBalanceCurrency() {
        return balanceCurrency;
    }

    public Double getAvailableTotal() {
        return availableTotal;
    }

    public String getAvailableTotalCurrency() {
        return availableTotalCurrency;
    }

    public Double getMaxBalanceTotal() {
        return maxBalanceTotal;
    }

    public String getMaxBalanceTotalCurrency() {
        return maxBalanceTotalCurrency;
    }

    public Double getNextBillingTotal() {
        return nextBillingTotal;
    }

    public String getBillingForm() {
        return billingForm;
    }

    public String getBillingText() {
        return billingText;
    }

    public String getCardNo() {
        return cardNo;
    }

    public String getCardUser() {
        return cardUser;
    }

    public String getCardName() {
        return cardName;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getMaxBalance() {
        return maxBalance;
    }

    public Double getNextBilling() {
        return nextBilling;
    }

    public Double getAvailable() {
        return available;
    }

    public String getCardStatus() {
        return cardStatus;
    }

    public boolean getStopped() {
        return stopped;
    }

    public String getLatestUpdate() {
        return latestUpdate;
    }

    public String getBillingAccountRegNo() {
        return billingAccountRegNo;
    }

    public String getBillingAccountAccountNo() {
        return billingAccountAccountNo;
    }

    public String getCardUserCprNo() {
        return cardUserCprNo;
    }
}
