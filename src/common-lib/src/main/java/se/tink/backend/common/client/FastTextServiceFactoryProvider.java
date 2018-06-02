package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.backend.guice.annotations.FastTextConfiguration;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class FastTextServiceFactoryProvider implements Provider<FastTextServiceFactory> {
    private final EndpointConfiguration endpoint;
    private final CuratorFramework coordinationClient;

    @Inject
    public FastTextServiceFactoryProvider(@FastTextConfiguration EndpointConfiguration endpoint,
            CuratorFramework coordinationClient) {
        this.endpoint = endpoint;
        this.coordinationClient = coordinationClient;
    }

    @Override
    public FastTextServiceFactory get() {
        Client client = JerseyUtils.getClient(endpoint.getPinnedCertificates());
        return new FastTextServiceFactory(new DiscoveredWebServiceClassBuilder(
                coordinationClient, client, FastTextServiceFactory.SERVICE_NAME));
    }
}
