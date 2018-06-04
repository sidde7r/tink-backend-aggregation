package se.tink.backend.client;

import se.tink.libraries.http.client.ServiceClassBuilder;

public class ClientServiceFactory implements ServiceFactory {

    private ServiceClassBuilder builder;
    private ClientAuthorizationConfigurator authenticationConfigurator;

    public ClientServiceFactory(ServiceClassBuilder builder, ClientAuthorizationConfigurator authenticationConfigurator) {
        this.builder = builder;
        this.authenticationConfigurator = authenticationConfigurator;
    }

}
