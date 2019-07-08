package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.monabanq;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MonabanqAgent extends CmcicAgent {
    public MonabanqAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return MonabanqConstants.INTEGRATION_NAME;
    }
}
