package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.creditoagricola;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.creditoagricola.CreditoAgricolaConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditoAgricolaAgent extends SibsBaseAgent {

    public CreditoAgricolaAgent(
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
