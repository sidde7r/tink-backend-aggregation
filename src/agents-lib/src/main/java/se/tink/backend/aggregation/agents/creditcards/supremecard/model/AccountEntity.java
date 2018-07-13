package se.tink.backend.aggregation.agents.creditcards.supremecard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    private String accountNumber;
    private String approvedCredit;
    private String name;
    private String positiveBalance;
    private String reservedAmount;
    private String usableAmount;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getApprovedCredit() {
        return approvedCredit;
    }

    public String getName() {
        return name;
    }

    public String getPositiveBalance() {
        return positiveBalance;
    }

    public String getReservedAmount() {
        return reservedAmount;
    }

    public String getUsableAmount() {
        return usableAmount;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setApprovedCredit(String approvedCredit) {
        this.approvedCredit = approvedCredit;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPositiveBalance(String positiveBalance) {
        this.positiveBalance = positiveBalance;
    }

    public void setReservedAmount(String reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public void setUsableAmount(String usableAmount) {
        this.usableAmount = usableAmount;
    }
}
