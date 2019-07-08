package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.ringkjobinglandbobank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class RingkjobingLandbobankAgent extends BankdataAgent {

    public RingkjobingLandbobankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return RingkjobingLandbobankConstants.BASE_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return RingkjobingLandbobankConstants.BASE_AUTH_URL;
    }
}
