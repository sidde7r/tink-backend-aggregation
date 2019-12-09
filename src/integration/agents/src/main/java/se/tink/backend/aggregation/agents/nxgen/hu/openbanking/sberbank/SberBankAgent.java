package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SberBankAgent extends FintechblocksAgent {

    public SberBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    protected String getIntegrationName() {
        return SberBankConstants.INTEGRATION_NAME;
    }
}
