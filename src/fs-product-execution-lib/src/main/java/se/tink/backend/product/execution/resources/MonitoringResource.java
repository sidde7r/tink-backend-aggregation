package se.tink.backend.product.execution.resources;

import se.tink.backend.product.execution.api.MonitoringService;

public class MonitoringResource implements MonitoringService {
    @Override
    public String ping() {
        return "pong";
    }
}
