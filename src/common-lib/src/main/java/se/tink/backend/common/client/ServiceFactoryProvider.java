package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.client.ClientAuthorizationConfigurator;
import se.tink.backend.client.ClientServiceFactory;
import se.tink.backend.client.ServiceFactory;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.jersey.utils.JerseyUtils;
import se.tink.backend.guice.annotations.MainConfiguration;

public class ServiceFactoryProvider implements Provider<ServiceFactory> {
    private final EndpointConfiguration endpoint;
    private final CuratorFramework coordinationClient;

    @Inject
    public ServiceFactoryProvider(@MainConfiguration EndpointConfiguration endpoint,
            CuratorFramework coordinationClient) {
        this.endpoint = endpoint;
        this.coordinationClient = coordinationClient;
    }

    @Override
    public ServiceFactory get() {
        Client mainClient = JerseyUtils.getClient(endpoint.getPinnedCertificates());
        ClientAuthorizationConfigurator authenticationConfigurator = ClientAuthorizationConfigurator
                .decorateAndInstantiate(mainClient);
        // Not provisioning API access key since main is accessible from the Internet. Requiring an access
        // token would break communication from external clients.
        return new ClientServiceFactory(new DiscoveredWebServiceClassBuilder(
                coordinationClient, mainClient, ServiceFactory.SERVICE_NAME),
                authenticationConfigurator);
    }
}
