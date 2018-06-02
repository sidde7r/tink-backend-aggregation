package se.tink.backend.insights.core.domain.model;

public interface InsightScore {
    // 1-100
    // 1 lowest
    // 100 highest
    // Should be calculated for each insight
    default int calculateInsightScore() {
        return 20;
    }
}
