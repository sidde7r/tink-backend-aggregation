package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BpmAgent extends CbiGlobeAgent {

    public BpmAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new BpmApiClient(client, persistentStorage);
    }

    @Override
    protected String getIntegrationName() {
        return BpmConstants.INTEGRATION_NAME;
    }
}
