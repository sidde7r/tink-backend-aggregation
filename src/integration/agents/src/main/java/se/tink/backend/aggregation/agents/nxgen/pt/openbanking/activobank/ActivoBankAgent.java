package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.activobank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.activobank.ActivoBankConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ActivoBankAgent extends SibsBaseAgent {

    public ActivoBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return Market.INTEGRATION_NAME;
    }
}
