package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpsondrio;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BPSondrioAgent extends CbiGlobeAgent {

    public BPSondrioAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return BPSondrioConstants.INTEGRATION_NAME;
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new BPSondrioApiClient(client, persistentStorage, requestManual, temporaryStorage);
    }
}
