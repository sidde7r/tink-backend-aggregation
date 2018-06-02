package se.tink.backend.core;

import java.util.List;

public class LoanTimelineResponse {

    private List<KVPair<String, Double>> weightedAverageTimeline;
    private List<LoanTimeline> loanTimelines;

    public List<KVPair<String, Double>> getWeightedAverageTimeline() {
        return weightedAverageTimeline;
    }

    public void setWeightedAverageTimeline(List<KVPair<String, Double>> weightedAverageTimeline) {
        this.weightedAverageTimeline = weightedAverageTimeline;
    }

    public List<LoanTimeline> getLoanTimelines() {
        return loanTimelines;
    }

    public void setLoanTimelines(
            List<LoanTimeline> loanTimelines) {
        this.loanTimelines = loanTimelines;
    }
}
