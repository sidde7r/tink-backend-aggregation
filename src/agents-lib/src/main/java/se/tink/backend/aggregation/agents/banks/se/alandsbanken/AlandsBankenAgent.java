package se.tink.backend.aggregation.agents.banks.se.alandsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.banks.crosskey.CrossKeyAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;
import se.tink.backend.core.enums.FeatureFlags;

public class AlandsBankenAgent extends CrossKeyAgent {

    public AlandsBankenAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new AlandsBankenConfig());

        apiClient.setRootUrl("https://mob.alandsbanken.se/cbs-inet-json-api-abs-v1/api/");
        apiClient.setErrorHandler(new AlandsBankenErrorHandler());
        apiClient.setLanguage("sv");

        if (request.getUser().getFlags().contains(FeatureFlags.ALANDSBANKEN_SE_V4)) {
            apiClient.setAppId("1.4.0-iOS");
        } else {
            apiClient.setAppId("1.3.2-iOS");
        }

    }
}
