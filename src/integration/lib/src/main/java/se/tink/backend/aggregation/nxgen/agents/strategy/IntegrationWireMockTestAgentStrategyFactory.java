package se.tink.backend.aggregation.nxgen.agents.strategy;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IntegrationWireMockTestAgentStrategyFactory implements AgentStrategyFactory {

    private final String wireMockServerHost;

    public IntegrationWireMockTestAgentStrategyFactory(String wireMockServerHost) {
        this.wireMockServerHost = wireMockServerHost;
    }

    @Override
    public SubsequentGenerationAgentStrategy build(
            CredentialsRequest credentialsRequest,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        return new IntegrationWireMockTestAgentStrategy(
                credentialsRequest, context, signatureKeyPair, wireMockServerHost);
    }
}
