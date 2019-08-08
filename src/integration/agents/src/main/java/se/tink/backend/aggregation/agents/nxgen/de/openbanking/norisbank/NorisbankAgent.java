package se.tink.backend.aggregation.agents.nxgen.de.openbanking.norisbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NorisbankAgent extends DeutscheBankAgent {

    public NorisbankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseURL() {
        return NorisbankConstants.BASE_URL;
    }

    @Override
    protected String getPSUIdType() {
        return NorisbankConstants.PSU_ID_TYPE;
    }
}
