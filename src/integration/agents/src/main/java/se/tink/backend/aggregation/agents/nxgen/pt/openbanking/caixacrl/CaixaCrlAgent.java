package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixacrl;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseSubsequentAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CaixaCrlAgent extends SibsBaseSubsequentAgent {

    public CaixaCrlAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return CaixaCrlConstants.INTEGRATION_NAME;
    }
}
