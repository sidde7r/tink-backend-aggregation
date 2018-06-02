package se.tink.analytics.jobs.loandata;

import se.tink.backend.core.interests.AggregatedAreaLoanData;

import java.util.UUID;

public class AggregatedAreaLoanDataExtended extends AggregatedAreaLoanData {
    private UUID userId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
