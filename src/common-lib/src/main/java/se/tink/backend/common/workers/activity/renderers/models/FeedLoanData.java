package se.tink.backend.common.workers.activity.renderers.models;

import se.tink.backend.core.LoanEvent;

public class FeedLoanData extends ActivityHeader {
    private LoanEvent loanEvent;

    public LoanEvent getLoanEvent() {
        return loanEvent;
    }

    public void setLoanEvent(LoanEvent loanEvent) {
        this.loanEvent = loanEvent;
    }
}
