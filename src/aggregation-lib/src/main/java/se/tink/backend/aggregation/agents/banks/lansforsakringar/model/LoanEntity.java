package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanEntity {
    private String loanName;
    private String loanNumber;
    private String remainingDebt;
    private boolean badDebt;

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    public String getRemainingDebt() {
        return remainingDebt;
    }

    public void setRemainingDebt(String remainingDebt) {
        this.remainingDebt = remainingDebt;
    }

    public boolean isBadDebt() {
        return badDebt;
    }

    public void setBadDebt(boolean badDebt) {
        this.badDebt = badDebt;
    }
}
