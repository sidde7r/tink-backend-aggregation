package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.WebResource;
import se.tink.backend.client.ClientAuthorizationConfigurator;
import se.tink.backend.client.ClientServiceFactory;
import se.tink.backend.client.ServiceFactory;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;

public class TestServiceFactoryProvider implements Provider<ServiceFactory> {

    private String wireMockUrl;

    @Inject
    public TestServiceFactoryProvider(@Named("wireMockUrl") String wireMockUrl) {
        this.wireMockUrl = wireMockUrl;
    }

    @Override
    public ServiceFactory get() {
        WebResource mainJerseyResource = InterContainerJerseyClientFactory.withoutPinning().build()
                .resource(wireMockUrl);
        ClientAuthorizationConfigurator authenticationConfigurator = ClientAuthorizationConfigurator
                .decorateAndInstantiate(mainJerseyResource);
        return new ClientServiceFactory(new BasicWebServiceClassBuilder(mainJerseyResource),
                authenticationConfigurator);
    }
}
