package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.alandsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsbankenFIAgent extends CrosskeyBaseAgent {

    public AlandsbankenFIAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getIntegrationName() {
        return AlandsbankenFIConstants.Market.INTEGRATION_NAME;
    }

    @Override
    public String getClientName() {
        return AlandsbankenFIConstants.Market.CLIENT_NAME;
    }
}
