package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsRequest {
    private String loanId;

    public LoanDetailsRequest(String loanId) {
        this.loanId = loanId;
    }
}
