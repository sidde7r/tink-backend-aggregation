package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sydbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SydbankAgent extends BankdataAgent {

    public SydbankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return SydbankConstants.BASE_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return SydbankConstants.BASE_AUTH_URL;
    }

    @Override
    protected String getIntegrationName() {
        return SydbankConstants.INTEGRATION_NAME;
    }
}
