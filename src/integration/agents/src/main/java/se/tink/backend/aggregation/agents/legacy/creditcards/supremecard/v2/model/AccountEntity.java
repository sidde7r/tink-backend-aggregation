package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    private String accountNumber;
    private String name;
    private String status;
    private String approvedCredit;
    private String negativeBalance;
    private String positiveBalance;
    private String reservedAmount;
    private String usableAmount;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApprovedCredit() {
        return approvedCredit;
    }

    public void setApprovedCredit(String approvedCredit) {
        this.approvedCredit = approvedCredit;
    }

    public String getNegativeBalance() {
        return negativeBalance;
    }

    public void setNegativeBalance(String negativeBalance) {
        this.negativeBalance = negativeBalance;
    }

    public String getPositiveBalance() {
        return positiveBalance;
    }

    public void setPositiveBalance(String positiveBalance) {
        this.positiveBalance = positiveBalance;
    }

    public String getReservedAmount() {
        return reservedAmount;
    }

    public void setReservedAmount(String reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public String getUsableAmount() {
        return usableAmount;
    }

    public void setUsableAmount(String usableAmount) {
        this.usableAmount = usableAmount;
    }
}
