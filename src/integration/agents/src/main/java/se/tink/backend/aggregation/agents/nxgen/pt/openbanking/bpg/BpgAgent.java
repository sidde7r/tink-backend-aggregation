package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.bpg;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.bpg.BpgConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BpgAgent extends SibsBaseAgent {

    public BpgAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getClientName() {
        return Market.CLIENT_NAME;
    }

    @Override
    protected String getIntegrationName() {
        return Market.INTEGRATION_NAME;
    }
}
