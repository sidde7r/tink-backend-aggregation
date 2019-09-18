package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.almbrand;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AlmBrandAgent extends BankdataAgent {

    public AlmBrandAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return AlmBrandConstants.BASE_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return AlmBrandConstants.BASE_AUTH_URL;
    }
}
