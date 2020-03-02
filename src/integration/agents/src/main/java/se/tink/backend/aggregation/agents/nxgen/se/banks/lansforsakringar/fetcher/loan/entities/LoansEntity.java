package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoansEntity {
    private String loanName;
    private String loanNumber;
    private String remainingDebt;
    private boolean badDebt;

    public String getLoanName() {
        return loanName;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public String getRemainingDebt() {
        return remainingDebt;
    }

    public boolean isBadDebt() {
        return badDebt;
    }
}
