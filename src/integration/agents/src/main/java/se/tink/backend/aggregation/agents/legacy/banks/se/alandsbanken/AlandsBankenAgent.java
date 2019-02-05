package se.tink.backend.aggregation.agents.banks.se.alandsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsBankenAgent extends CrossKeyAgent {

    public AlandsBankenAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AlandsBankenConfig());

        apiClient.setRootUrl("https://mob.alandsbanken.se/cbs-inet-json-api-abs-v1/api/");
        apiClient.setErrorHandler(new AlandsBankenErrorHandler());
        apiClient.setLanguage("sv");

        apiClient.setAppId("1.4.0-iOS");
    }
}
