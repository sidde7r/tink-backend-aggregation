package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LaBanquePostaleAgent extends OpenBankProjectAgent {

    public LaBanquePostaleAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getIntegrationName() {
        return Market.INTEGRATION_NAME;
    }

    @Override
    public String getClientName() {
        return Market.CLIENT_NAME;
    }
}
