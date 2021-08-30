package se.tink.backend.aggregation.nxgen.agents.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public final class ProxyModule extends AbstractModule {

    @Inject
    @Singleton
    @Provides
    public TinkHttpClient provideProxy(
            AgentComponentProvider componentProvider, AgentsServiceConfiguration configuration) {
        String userId = componentProvider.getCredentialsRequest().getCredentials().getUserId();
        return new ProxyConfigurator(configuration)
                .assignProxyForUser(componentProvider.getTinkHttpClient(), userId);
    }
}
