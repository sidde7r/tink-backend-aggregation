package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.bancobpi;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoBpiAgent extends SibsBaseAgent {

    public BancoBpiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getClientName() {
        return BancoBpiConstants.Market.CLIENT_NAME;
    }

    @Override
    public String getIntegrationName() {
        return BancoBpiConstants.Market.INTEGRATION_NAME;
    }
}
