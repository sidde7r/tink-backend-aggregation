package se.tink.backend.aggregation.provider.configuration.client;

import se.tink.backend.aggregation.provider.configuration.api.MonitoringService;
import se.tink.backend.aggregation.provider.configuration.api.ProviderService;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.http.client.ServiceClassBuilder;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;

public class ClientProviderServiceFactory implements ProviderServiceFactory{

    private ServiceClassBuilder builder;

    public static ClientProviderServiceFactory buildWithoutPinning(String url) {
        return new ClientProviderServiceFactory(url);
    }

    private ClientProviderServiceFactory(String url) {
        this(new BasicWebServiceClassBuilder(
                InterContainerJerseyClientFactory.withoutPinning().build().resource(url)));
    }

    public ClientProviderServiceFactory(ServiceClassBuilder builder) {
        this.builder = builder;
    }

    @Override
    public ProviderService getProviderSerive() {
        return builder.build(ProviderService.class);
    }

    @Override
    public MonitoringService getMonitoringService() {
        return builder.build(MonitoringService.class);
    }
}
