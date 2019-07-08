package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordfyns;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NordfynsAgent extends BankdataAgent {

    public NordfynsAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return NordfynsConstants.BASE_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return NordfynsConstants.BASE_AUTH_URL;
    }
}
