package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.WebResource;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.jersey.utils.JerseyUtils;
import se.tink.backend.encryption.client.ClientEncryptionServiceFactory;
import se.tink.backend.encryption.client.EncryptionServiceFactory;
import se.tink.backend.guice.annotations.EncryptionConfiguration;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;

public class HostEncryptionServiceFactoryProvider implements Provider<EncryptionServiceFactory> {
    private final EndpointConfiguration endpoint;

    @Inject
    public HostEncryptionServiceFactoryProvider(@EncryptionConfiguration EndpointConfiguration endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public EncryptionServiceFactory get() {
        WebResource jerseyResource = JerseyUtils.getResource(endpoint.getPinnedCertificates(),
                endpoint.getUrl());
        JerseyUtils.registerAPIAccessToken(jerseyResource, endpoint.getAccessToken());
        return new ClientEncryptionServiceFactory(new BasicWebServiceClassBuilder(jerseyResource));
    }
}
