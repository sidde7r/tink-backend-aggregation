package se.tink.backend.insights.http.dto;

import java.util.List;

public class GetInsightsResponse {
    private List<InsightDTO> insights;

    public GetInsightsResponse(List<InsightDTO> insights) {
        this.insights = insights;
    }

    public List<InsightDTO> getInsights() {
        return insights;
    }
}
