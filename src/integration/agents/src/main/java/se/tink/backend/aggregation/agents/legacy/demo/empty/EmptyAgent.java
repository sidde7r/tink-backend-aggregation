package se.tink.backend.aggregation.agents.demo.empty;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class EmptyAgent extends NextGenerationAgent {
    @Inject
    public EmptyAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new EmptyAgentAuthenticator();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SessionHandler() {
            @Override
            public void logout() {
                // nop
            }

            @Override
            public void keepAlive() throws SessionException {
                // nop
            }
        };
    }
}
