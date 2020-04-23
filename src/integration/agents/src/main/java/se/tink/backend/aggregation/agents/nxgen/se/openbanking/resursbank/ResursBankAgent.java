package se.tink.backend.aggregation.agents.nxgen.se.openbanking.resursbank;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.resursbank.ResursBankConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ResursBankAgent extends CrosskeyBaseAgent {

    public ResursBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getIntegrationName() {
        return Market.INTEGRATION_NAME;
    }

    @Override
    public String getClientName() {
        return Market.CLIENT_NAME;
    }

    @Override
    protected String getxFapiFinancialId() {
        return ResursBankConstants.X_FAPI_FINANCIAL_ID;
    }
}
