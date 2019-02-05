package se.tink.backend.aggregation.provider.configuration.http.resources;

import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;

public class MonitoringServiceResource implements MonitoringService {
    @Override
    public String ping() {
        return "pong";
    }
}
