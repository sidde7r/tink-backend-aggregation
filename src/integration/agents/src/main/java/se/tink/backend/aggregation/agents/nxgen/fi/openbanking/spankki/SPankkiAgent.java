package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SPankkiAgent extends CrosskeyBaseAgent {

    private static final String X_FAPI_FINANCIAL_ID = "s-pankki";

    public SPankkiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getxFapiFinancialId() {
        return X_FAPI_FINANCIAL_ID;
    }
}
