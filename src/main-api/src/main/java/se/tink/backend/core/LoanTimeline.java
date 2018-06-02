package se.tink.backend.core;

import java.util.List;

public class LoanTimeline {
    private String accountId;
    private List<TemporalValue<Double>> interestRateTimeline;
    private List<TemporalValue<Double>> balanceTimeline;

    public List<TemporalValue<Double>> getInterestRateTimeline() {
        return interestRateTimeline;
    }

    public void setInterestRateTimeline(List<TemporalValue<Double>> interestRateTimeline) {
        this.interestRateTimeline = interestRateTimeline;
    }

    public List<TemporalValue<Double>> getBalanceTimeline() {
        return balanceTimeline;
    }

    public void setBalanceTimeline(List<TemporalValue<Double>> balanceTimeline) {
        this.balanceTimeline = balanceTimeline;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
