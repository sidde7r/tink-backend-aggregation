package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ProductionAgentStrategyFactory implements AgentStrategyFactory {

    @Override
    public SubsequentGenerationAgentStrategy build(
            CredentialsRequest credentialsRequest,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {

        return new NextGenSubsequentGenerationAgentStrategy(
                credentialsRequest, context, signatureKeyPair);
    }
}
