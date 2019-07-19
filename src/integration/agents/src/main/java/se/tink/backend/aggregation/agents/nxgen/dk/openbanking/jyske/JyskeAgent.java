package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.jyske;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class JyskeAgent extends BankdataAgent {

    public JyskeAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return JyskeConstants.BASE_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return JyskeConstants.BASE_AUTH_URL;
    }

    @Override
    protected String getIntegrationName() {
        return JyskeConstants.INTEGRATION_NAME;
    }
}
