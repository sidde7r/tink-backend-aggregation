package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetLoanDetailsBodyEntity {
    @JsonProperty("Detalhe")
    private LoanBodyDetailsEntity loanDetails;

    public LoanBodyDetailsEntity getLoanDetails() {
        return loanDetails;
    }
}
