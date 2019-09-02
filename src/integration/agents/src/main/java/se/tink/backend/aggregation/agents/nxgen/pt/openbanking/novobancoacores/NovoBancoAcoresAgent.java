package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.novobancoacores;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseSubsequentAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NovoBancoAcoresAgent extends SibsBaseSubsequentAgent {

    public NovoBancoAcoresAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return NovoBancoAcoresConstants.INTEGRATION_NAME;
    }
}
