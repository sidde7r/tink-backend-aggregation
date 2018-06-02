package se.tink.backend.core;

import java.util.List;

public class LoanEventsResponse {
    private List<LoanEvent> loanEvents;

    public List<LoanEvent> getLoanEvents() {
        return loanEvents;
    }

    public void setLoanEvents(List<LoanEvent> loanEvents) {
        this.loanEvents = loanEvents;
    }
}
