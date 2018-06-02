package se.tink.backend.insights.http;

public class InsightsMonitoringServiceResource implements InsightsMonitoringService{
    @Override
    public String ping() {
        return "pong";
    }
}
