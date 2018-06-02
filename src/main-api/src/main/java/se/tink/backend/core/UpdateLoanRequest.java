package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateLoanRequest {
    private String accountId;
    private Loan.Type loanType;
    private Double interest;
    private Double balance;

    public Double getInterest() {
        return interest;
    }

    public void setInterest(Double interest) {
        this.interest = interest;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Loan.Type getLoanType() {
        return loanType;
    }

    public void setLoanType(Loan.Type loanType) {
        this.loanType = loanType;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}
