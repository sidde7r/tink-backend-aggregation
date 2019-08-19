package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class DeutscheBankDEAgent extends DeutscheBankAgent {

    public DeutscheBankDEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseURL() {
        return DeutscheBankDEConstants.BASE_URL;
    }

    @Override
    protected String getPSUIdType() {
        return DeutscheBankDEConstants.PSU_ID_TYPE;
    }
}
