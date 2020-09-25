package se.tink.backend.aggregation.agents.banks.se.marginalenbank;

import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class MarginalenBankAgent extends CrossKeyAgent {
    public MarginalenBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new MarginalenBankConfig());

        apiClient.setRootUrl("https://secure5.marginalen.se/cbs-inet-json-api-mba-v1/api/");
        apiClient.setErrorHandler(new MarginalenBankErrorHandler());
        apiClient.setAppId("1.0.0-iOS");
        apiClient.setLanguage("sv");
    }
}
