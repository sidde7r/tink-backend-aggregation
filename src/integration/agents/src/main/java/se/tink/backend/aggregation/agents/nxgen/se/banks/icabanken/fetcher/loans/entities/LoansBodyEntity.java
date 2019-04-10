package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansBodyEntity {
    @JsonProperty("LoanList")
    private LoanListEntity loanList;

    @JsonProperty("MortgageList")
    private MortgageListEntity mortgageList;

    public LoanListEntity getLoanList() {
        return loanList;
    }

    public MortgageListEntity getMortgageList() {
        return mortgageList;
    }
}
