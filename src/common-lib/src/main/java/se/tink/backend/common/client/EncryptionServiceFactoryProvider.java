package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import org.apache.curator.framework.CuratorFramework;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.jersey.utils.JerseyUtils;
import se.tink.backend.encryption.client.ClientEncryptionServiceFactory;
import se.tink.backend.encryption.client.EncryptionServiceFactory;
import se.tink.backend.guice.annotations.EncryptionConfiguration;

public class EncryptionServiceFactoryProvider implements Provider<EncryptionServiceFactory> {
    private final EndpointConfiguration endpoint;
    private final CuratorFramework coordinationClient;

    @Inject
    public EncryptionServiceFactoryProvider(@EncryptionConfiguration EndpointConfiguration endpoint,
            CuratorFramework coordinationClient) {
        this.endpoint = endpoint;
        this.coordinationClient = coordinationClient;
    }

    @Override
    public EncryptionServiceFactory get() {
        Client client = JerseyUtils.getClient(endpoint.getPinnedCertificates());
        JerseyUtils.registerAPIAccessToken(client, endpoint.getAccessToken());
        return new ClientEncryptionServiceFactory(new DiscoveredWebServiceClassBuilder(
                coordinationClient, client, EncryptionServiceFactory.SERVICE_NAME));
    }
}
