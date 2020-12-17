package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.session;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public final class MetroSessionHandlerModule extends AbstractModule {

    @Singleton
    @Inject
    @Provides
    public SessionHandler sessionHandler(AgentPlatformHttpClient httpClient) {
        SessionClient sessionClient = new SessionClient(httpClient);
        return new MetroSessionHandler(sessionClient);
    }
}
