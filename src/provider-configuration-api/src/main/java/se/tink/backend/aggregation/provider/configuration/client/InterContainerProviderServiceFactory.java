package se.tink.backend.aggregation.provider.configuration.client;

import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;

public interface InterContainerProviderServiceFactory {
    String SERVICE_NAME = "provider-configuration";

    ProviderService getProviderService(String clusterName, String clusterEnvironment);
    MonitoringService getMonitoringService();
}
