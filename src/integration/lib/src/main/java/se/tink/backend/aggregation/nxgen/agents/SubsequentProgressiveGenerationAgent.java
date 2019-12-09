package se.tink.backend.aggregation.nxgen.agents;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class SubsequentProgressiveGenerationAgent
        extends SubsequentGenerationAgent<StatelessProgressiveAuthenticator>
        implements ProgressiveAuthAgent {

    protected SubsequentProgressiveGenerationAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    protected SubsequentProgressiveGenerationAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            boolean useNextGenClient) {
        super(request, context, signatureKeyPair, useNextGenClient);
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
