package se.tink.backend.aggregation.credit.safe.resource;

import se.tink.backend.aggregation.credit.safe.api.MonitoringService;

public class MonitoringServiceResource implements MonitoringService {
    @Override
    public String ping() {
        return "pong";
    }
}
