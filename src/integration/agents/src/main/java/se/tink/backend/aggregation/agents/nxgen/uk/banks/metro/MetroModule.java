package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.MetroAuthenticationModule;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.session.MetroSessionHandlerModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.handler.AgentPlatformHttpResponseStatusHandler;

public class MetroModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new MetroAuthenticationModule());
        install(new MetroSessionHandlerModule());
    }

    @Inject
    @Singleton
    @Provides
    public AgentPlatformHttpClient agentPlatformHttpClient(
            AgentComponentProvider componentProvider) {
        TinkHttpClient tinkHttpClient = componentProvider.getTinkHttpClient();
        tinkHttpClient.setResponseStatusHandler(new AgentPlatformHttpResponseStatusHandler());
        tinkHttpClient.disableSignatureRequestHeader();
        return new AgentPlatformHttpClient(tinkHttpClient);
    }
}
