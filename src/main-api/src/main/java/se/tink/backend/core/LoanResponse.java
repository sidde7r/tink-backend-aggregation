package se.tink.backend.core;

import java.util.List;

public class LoanResponse {
    private final List<Loan> loans;
    private Double totalLoanAmount;
    private Double weightedAverageInterestRate;

    public LoanResponse(List<Loan> loans) {
        this.loans = loans;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public Double getTotalLoanAmount() {
        return totalLoanAmount;
    }

    public void setTotalLoanAmount(Double totalLoanValue) {
        this.totalLoanAmount = totalLoanValue;
    }

    public Double getWeightedAverageInterestRate() {
        return weightedAverageInterestRate;
    }

    public void setWeightedAverageInterestRate(Double weightedAverageInterestRate) {
        this.weightedAverageInterestRate = weightedAverageInterestRate;
    }

}
