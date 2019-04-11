package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bnpparibas;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.OpenBankProjectAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BnpParibasAgent extends OpenBankProjectAgent {

    private final String clientName;

    public BnpParibasAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        clientName = request.getProvider().getPayload();
    }

    @Override
    public String getIntegrationName() {
        return BnpParibasConstants.INTEGRATION_NAME;
    }

    @Override
    public String getClientName() {
        return clientName;
    }
}
