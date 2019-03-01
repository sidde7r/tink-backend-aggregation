package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsRequest {
    @JsonProperty("refValContrato")
    private String loanId;

    public LoanDetailsRequest(String loanId) {
        this.loanId = loanId;
    }
}
