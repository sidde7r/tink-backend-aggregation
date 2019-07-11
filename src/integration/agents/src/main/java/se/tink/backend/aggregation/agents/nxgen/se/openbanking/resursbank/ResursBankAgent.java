package se.tink.backend.aggregation.agents.nxgen.se.openbanking.resursbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ResursBankAgent extends CrosskeyBaseAgent {

    public ResursBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getIntegrationName() {
        return ResursBankConstants.Market.INTEGRATION_NAME;
    }

    @Override
    public String getClientName() {
        return ResursBankConstants.Market.CLIENT_NAME;
    }

    @Override
    protected String getBaseAPIUrl() {
        return ResursBankConstants.BASE_API_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return ResursBankConstants.BASE_AUTH_URL;
    }

    @Override
    protected String getxFapiFinancialId() {
        return ResursBankConstants.X_FAPI_FINANCIAL_ID;
    }
}
