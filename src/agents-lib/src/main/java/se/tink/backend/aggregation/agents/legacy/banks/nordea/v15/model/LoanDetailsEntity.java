package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanDetailsEntity {

    private LoanData loanData;
    private LoanPaymentDetails followingPayment;
    private LoanPaymentDetails latestPayment;

    public LoanPaymentDetails getFollowingPayment() {
        return followingPayment;
    }

    public void setFollowingPayment(LoanPaymentDetails followingPayment) {
        this.followingPayment = followingPayment;
    }

    public LoanPaymentDetails getLatestPayment() {
        return latestPayment;
    }

    public void setLatestPayment(LoanPaymentDetails latestPayment) {
        this.latestPayment = latestPayment;
    }

    public LoanData getLoanData() {
        return loanData;
    }

    public void setLoanData(LoanData loanData) {
        this.loanData = loanData;
    }
}
