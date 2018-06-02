package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import org.apache.curator.framework.CuratorFramework;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.jersey.utils.JerseyUtils;
import se.tink.backend.guice.annotations.SystemConfiguration;
import se.tink.backend.system.client.ClientSystemServiceFactory;
import se.tink.backend.system.client.SystemServiceFactory;

public class SystemServiceFactoryProvider implements Provider<SystemServiceFactory> {
    private final EndpointConfiguration endpoint;
    private final CuratorFramework coordinationClient;

    @Inject
    public SystemServiceFactoryProvider(@SystemConfiguration EndpointConfiguration endpoint,
            CuratorFramework coordinationClient) {
        this.endpoint = endpoint;
        this.coordinationClient = coordinationClient;
    }

    @Override
    public SystemServiceFactory get() {
        Client client = JerseyUtils.getClient(endpoint.getPinnedCertificates());
        JerseyUtils.registerAPIAccessToken(client, endpoint.getAccessToken());
        return new ClientSystemServiceFactory(new DiscoveredWebServiceClassBuilder(
                coordinationClient, client, SystemServiceFactory.SERVICE_NAME));
    }
}
