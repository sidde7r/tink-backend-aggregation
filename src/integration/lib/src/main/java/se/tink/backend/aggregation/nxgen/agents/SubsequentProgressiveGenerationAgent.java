package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;

public abstract class SubsequentProgressiveGenerationAgent
        extends SubsequentGenerationAgent<StatelessProgressiveAuthenticator>
        implements ProgressiveAuthAgent {

    protected SubsequentProgressiveGenerationAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        return getAuthenticator().processAuthentication(request);
    }

    @Override
    public final boolean login() throws Exception {
        throw new AssertionError("ProgressiveAuthAgent::login should always be used");
    }
}
