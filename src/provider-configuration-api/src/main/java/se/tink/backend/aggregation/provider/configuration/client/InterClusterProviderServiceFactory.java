package se.tink.backend.aggregation.provider.configuration.client;

import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;

public interface InterClusterProviderServiceFactory {
    String SERVICE_NAME = "provider-configuration";

    ProviderService getProviderService();
    MonitoringService getMonitoringService();
}
