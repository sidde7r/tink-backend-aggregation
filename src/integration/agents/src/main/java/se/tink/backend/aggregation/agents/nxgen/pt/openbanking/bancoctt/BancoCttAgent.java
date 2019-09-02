package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.bancoctt;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseSubsequentAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoCttAgent extends SibsBaseSubsequentAgent {

    public BancoCttAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return BancoCttConstants.INTEGRATION_NAME;
    }
}
