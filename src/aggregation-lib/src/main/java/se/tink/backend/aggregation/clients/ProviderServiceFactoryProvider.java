package se.tink.backend.aggregation.clients;

import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.aggregation.provider.configuration.client.InterContainerClientProviderServiceFactory;
import se.tink.backend.aggregation.provider.configuration.client.InterContainerProviderServiceFactory;
import se.tink.backend.guice.annotations.ProviderConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;

// TODO decide what should be in cluster name and environment when requesting from aggregation service
public class ProviderServiceFactoryProvider implements Provider<InterContainerProviderServiceFactory> {
    private final EndpointConfiguration endpointConfiguration;

    @Inject
    public ProviderServiceFactoryProvider(@ProviderConfiguration EndpointConfiguration providerConfiguration){
        this.endpointConfiguration = providerConfiguration;
    }

    @Override
    public InterContainerProviderServiceFactory get() {
        if (endpointConfiguration == null) {
            throw new IllegalStateException("no endpoint found, did we configure correctly?");
        }

        return new InterContainerClientProviderServiceFactory(
                endpointConfiguration.getPinnedCertificates(),
                endpointConfiguration.getUrl(),
                endpointConfiguration.getAccessToken()
        );
    }
}
