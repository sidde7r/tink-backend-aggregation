package se.tink.backend.core;

import java.util.List;

public class LoanEventActivityData {
    private String loanType;
    private Double currentInterestRate;
    private Double interestRateChange;
    private Double balance;
    private List<LoanEvent> loanEvents;     // For now, they are all from the same date
    private boolean challengeLoanEligible;

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String type) {
        loanType = type;
    }

    public Double getCurrentInterestRate() {
        return currentInterestRate;
    }

    public void setCurrentInterestRate(Double currentInterestRate) {
        this.currentInterestRate = currentInterestRate;
    }

    public Double getInterestRateChange() {
        return interestRateChange;
    }

    public void setInterestRateChange(Double interestRateChange) {
        this.interestRateChange = interestRateChange;
    }

    public List<LoanEvent> getLoanEvents() {
        return loanEvents;
    }

    public void setLoanEvents(List<LoanEvent> loanEvents) {
        this.loanEvents = loanEvents;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public boolean isChallengeLoanEligible() {
        return challengeLoanEligible;
    }

    public void setChallengeLoanEligible(boolean challengeLoanEligible) {
        this.challengeLoanEligible = challengeLoanEligible;
    }
}
