package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** @deprecated Class is replaced by dependency injection of AgentStrategyFactory. */
@Deprecated
public final class SubsequentGenerationAgentStrategyFactory {

    private SubsequentGenerationAgentStrategyFactory() {
        throw new AssertionError();
    }

    @Deprecated
    public static SubsequentGenerationAgentStrategy nxgen(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        return new NextGenSubsequentGenerationAgentStrategy(
                credentialsRequest, context, signatureKeyPair);
    }

    @Deprecated
    public static SubsequentGenerationAgentStrategy legacy(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        return new LegacySubsequentGenerationAgentStrategy(
                credentialsRequest, context, signatureKeyPair);
    }
}
