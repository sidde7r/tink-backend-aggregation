package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchLoanDetailsRequest {
    private String loanNumber;

    private FetchLoanDetailsRequest(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    @JsonIgnore
    public static FetchLoanDetailsRequest of(String loanNumber) {
        return new FetchLoanDetailsRequest(loanNumber);
    }
}
