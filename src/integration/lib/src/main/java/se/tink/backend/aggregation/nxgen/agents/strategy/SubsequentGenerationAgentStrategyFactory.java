package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SubsequentGenerationAgentStrategyFactory {

    private SubsequentGenerationAgentStrategyFactory() {
        throw new AssertionError();
    }

    /** @deprecated This strategy should eventually be removed. Use nxgen() instead. */
    @Deprecated
    public static SubsequentGenerationAgentStrategy legacy(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        return new LegacySubsequentGenerationAgentStrategy(
                credentialsRequest, context, signatureKeyPair);
    }

    public static SubsequentGenerationAgentStrategy nxgen(
            final CredentialsRequest credentialsRequest,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        return new NextGenSubsequentGenerationAgentStrategy(
                credentialsRequest, context, signatureKeyPair);
    }

    /**
     * @deprecated This switching method should eventually be removed. Just use nxgen() directly.
     */
    public static SubsequentGenerationAgentStrategy create(
            CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair,
            final boolean useNextGenClient) {
        if (useNextGenClient) {
            return nxgen(request, context, signatureKeyPair);
        }
        return legacy(request, context, signatureKeyPair);
    }
}
