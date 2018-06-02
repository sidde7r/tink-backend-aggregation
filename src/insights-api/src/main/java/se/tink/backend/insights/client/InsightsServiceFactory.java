package se.tink.backend.insights.client;

import se.tink.backend.insights.http.InsightService;

public interface InsightsServiceFactory {
    String SERVICE_NAME = "insights";

    InsightService getInsightsService();
}
