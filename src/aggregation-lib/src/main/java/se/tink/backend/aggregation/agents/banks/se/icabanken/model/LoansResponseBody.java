package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoansResponseBody {

    @JsonProperty("LoanList")
    private LoanListEntity loanList;

    @JsonProperty("MortgageList")
    private MortgageListEntity mortgageList;

    public LoanListEntity getLoanList() {
        return loanList;
    }

    public void setLoanList(LoanListEntity loanList) {
        this.loanList = loanList;
    }

    public MortgageListEntity getMortgageList() {
        return mortgageList;
    }

    public void setMortgageList(MortgageListEntity mortgageList) {
        this.mortgageList = mortgageList;
    }
}
