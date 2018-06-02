package se.tink.backend.insights.renderer.controller;

import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.insights.core.domain.model.Insight;
import se.tink.backend.insights.renderer.BaseInsightRenderer;

public class InsightRenderer {

    private BaseInsightRenderer baseInsightRenderer;

    @Inject
    public InsightRenderer(BaseInsightRenderer baseInsightRenderer) {
        this.baseInsightRenderer = baseInsightRenderer;
    }

    public List<String> renderAllInsights(List<Insight> insights) {

        return insights.stream()
                .map(baseInsightRenderer::render).collect(Collectors.toList());
    }
}
