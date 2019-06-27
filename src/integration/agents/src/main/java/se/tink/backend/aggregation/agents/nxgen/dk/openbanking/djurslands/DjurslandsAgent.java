package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.djurslands;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class DjurslandsAgent extends BankdataAgent {

    public DjurslandsAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return DjurslandsConstants.BASE_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return DjurslandsConstants.BASE_AUTH_URL;
    }
}