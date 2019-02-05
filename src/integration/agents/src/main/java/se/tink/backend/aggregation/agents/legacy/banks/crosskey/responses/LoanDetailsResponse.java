package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanDetailsResponse extends BaseResponse {
    private CrossKeyLoanDetails loanDetailsVO;

    public CrossKeyLoanDetails getLoanDetailsVO() {
        return loanDetailsVO;
    }

    public void setLoanDetailsVO(CrossKeyLoanDetails loanDetailsVO) {
        this.loanDetailsVO = loanDetailsVO;
    }
}
