package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.categorization.client.CategorizationServiceFactory;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class CategorizationServiceFactoryProvider implements Provider<CategorizationServiceFactory> {
    private final EndpointConfiguration endpoint;
    private final CuratorFramework coordinationClient;

    @Inject
    public CategorizationServiceFactoryProvider(@Named("CategorizerConfiguration") EndpointConfiguration endpoint,
            CuratorFramework coordinationClient) {
        this.endpoint = endpoint;
        this.coordinationClient = coordinationClient;
    }

    @Override
    public CategorizationServiceFactory get() {
        Client client = JerseyUtils.getClient(endpoint.getPinnedCertificates());
        return new CategorizationServiceFactory(new DiscoveredWebServiceClassBuilder(
                coordinationClient, client, CategorizationServiceFactory.SERVICE_NAME));
    }
}
