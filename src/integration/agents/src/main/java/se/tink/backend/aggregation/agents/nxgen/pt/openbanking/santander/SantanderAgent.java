package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.santander;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SantanderAgent extends SibsBaseAgent {

    public SantanderAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getClientName() {
        return SantanderConstants.Market.CLIENT_NAME;
    }

    @Override
    public String getIntegrationName() {
        return SantanderConstants.Market.INTEGRATION_NAME;
    }
}
