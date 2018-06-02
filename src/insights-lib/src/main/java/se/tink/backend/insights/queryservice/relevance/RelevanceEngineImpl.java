package se.tink.backend.insights.queryservice.relevance;

import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.utils.ComparatorUtils;

import java.util.List;
import java.util.stream.Collectors;

public class RelevanceEngineImpl implements RelevanceEngine {
    @Override
    public List<Insight> order(List<Insight> unordered) {
        return calculateRelevance(unordered)
                .stream()
                .sorted(ComparatorUtils.INSIGHT_BY_SCORE_DESC)
                .collect(Collectors.toList());
    }

    @Override
    public List<Insight> calculateRelevance(List<Insight> insights) {
        return insights.stream().map(this::calculateRelevance).collect(Collectors.toList());
    }

    @Override
    public Insight calculateRelevance(Insight insight) {
        insight.setRelevanceScore(insight.getTinkInsightScore().value() * 0.5 + insight.calculateInsightScore() * 0.5);
        return insight;
    }
}
