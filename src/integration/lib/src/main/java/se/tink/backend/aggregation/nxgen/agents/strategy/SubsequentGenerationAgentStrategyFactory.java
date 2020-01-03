package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SubsequentGenerationAgentStrategyFactory {

    private SubsequentGenerationAgentStrategyFactory() {
        throw new AssertionError();
    }

    public static SubsequentGenerationAgentStrategy nxgen(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        return new NextGenSubsequentGenerationAgentStrategy(
                credentialsRequest, context, signatureKeyPair);
    }
}
