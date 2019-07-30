package se.tink.backend.aggregation.agents.nxgen.it.openbanking.credem;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CredemAgent extends CbiGlobeAgent {

    public CredemAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return CredemConstants.INTEGRATION_NAME;
    }
}
