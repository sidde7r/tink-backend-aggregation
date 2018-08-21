package se.tink.backend.aggregation.clients;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.provider.configuration.client.ClientProviderServiceFactory;
import se.tink.backend.aggregation.provider.configuration.client.ProviderServiceFactory;
import se.tink.backend.guice.annotations.ProviderConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;

// TODO decide what should be in cluster name and environment when requesting from aggregation service
public class ProviderServiceFactoryProvider implements Provider<ProviderServiceFactory> {
    private final EndpointConfiguration endpointConfiguration;

    @Inject
    public ProviderServiceFactoryProvider(@ProviderConfiguration EndpointConfiguration providerConfiguration){
        this.endpointConfiguration = providerConfiguration;
    }

    @Override
    public ProviderServiceFactory get() {
        if (endpointConfiguration == null) {
            throw new IllegalStateException("no endpoint found, did we configure correctly?");
        }

        return new ClientProviderServiceFactory(
                endpointConfiguration.getPinnedCertificates(),
                endpointConfiguration.getUrl(),
                endpointConfiguration.getAccessToken()
        );
    }
}
