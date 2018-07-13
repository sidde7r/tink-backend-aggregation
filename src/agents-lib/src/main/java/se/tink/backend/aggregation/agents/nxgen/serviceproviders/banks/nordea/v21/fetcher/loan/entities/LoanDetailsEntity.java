package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsEntity {
    private LoanData loanData;
    private LoanPaymentDetails followingPayment;
    private LoanPaymentDetails latestPayment;

    public LoanPaymentDetails getFollowingPayment() {
        return followingPayment;
    }

    public LoanPaymentDetails getLatestPayment() {
        return latestPayment;
    }

    public LoanData getLoanData() {
        return loanData;
    }
}
