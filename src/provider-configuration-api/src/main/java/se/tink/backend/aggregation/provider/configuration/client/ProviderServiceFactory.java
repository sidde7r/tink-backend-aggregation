package se.tink.backend.aggregation.provider.configuration.client;

import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;

public interface ProviderServiceFactory {
    String SERVICE_NAME = "provider-contiguration";

    ProviderService getProviderSerive();
    MonitoringService getMonitoringService();
}
