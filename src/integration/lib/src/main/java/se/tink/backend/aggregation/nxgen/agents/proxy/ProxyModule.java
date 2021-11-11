package se.tink.backend.aggregation.nxgen.agents.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class ProxyModule extends AbstractModule {

    @Inject
    @Singleton
    @Provides
    public TinkHttpClient provideProxy(
            AgentComponentProvider componentProvider, AgentsServiceConfiguration configuration) {
        CredentialsRequest credentialsRequest = componentProvider.getCredentialsRequest();
        Provider provider = credentialsRequest.getProvider();
        String userId = credentialsRequest.getCredentials().getUserId();
        return new ProxyConfigurator(configuration)
                .assignProxyForUser(componentProvider.getTinkHttpClient(), provider, userId);
    }
}
