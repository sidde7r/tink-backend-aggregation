package se.tink.backend.insights.queryservice.relevance;

import se.tink.backend.insights.core.domain.model.Insight;

import java.util.List;

public interface RelevanceEngine {
    List<Insight> order(List<Insight> unordered);
    List<Insight> calculateRelevance(List<Insight> insights);
    Insight calculateRelevance(Insight insight);
}
