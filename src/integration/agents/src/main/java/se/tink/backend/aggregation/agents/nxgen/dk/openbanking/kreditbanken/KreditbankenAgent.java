package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.kreditbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class KreditbankenAgent extends BankdataAgent {

    public KreditbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return KreditbankenConstants.BASE_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return KreditbankenConstants.BASE_AUTH_URL;
    }

    @Override
    protected String getIntegrationName() {
        return KreditbankenConstants.INTEGRATION_NAME;
    }
}
