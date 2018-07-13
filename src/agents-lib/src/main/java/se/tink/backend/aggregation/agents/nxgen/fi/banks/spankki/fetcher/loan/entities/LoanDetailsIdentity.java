package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsIdentity {
    private String loanType;
    private String loanId;

    public LoanDetailsIdentity setLoanType(String loanType) {
        this.loanType = loanType;
        return this;
    }

    public LoanDetailsIdentity setLoanId(String loanId) {
        this.loanId = loanId;
        return this;
    }
}
