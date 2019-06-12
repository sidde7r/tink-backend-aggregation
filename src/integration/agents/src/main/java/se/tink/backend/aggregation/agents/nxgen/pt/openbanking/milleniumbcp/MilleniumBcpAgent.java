package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.milleniumbcp;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MilleniumBcpAgent extends SibsBaseAgent {

    public MilleniumBcpAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return MilleniumBcpConstants.INTEGRATION_NAME;
    }
}
