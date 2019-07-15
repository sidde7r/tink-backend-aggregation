package se.tink.backend.aggregation.agents.nxgen.se.openbanking.alandsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsBankAgent extends CrosskeyBaseAgent {

    public AlandsBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getIntegrationName() {
        return AlandsBankConstants.Market.INTEGRATION_NAME;
    }

    @Override
    public String getClientName() {
        return AlandsBankConstants.Market.CLIENT_NAME;
    }

    @Override
    protected String getxFapiFinancialId() {
        return AlandsBankConstants.X_FAPI_FINANCIAL_ID;
    }

    @Override
    protected String getBaseAPIUrl() {
        return AlandsBankConstants.BASE_API_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return AlandsBankConstants.BASE_AUTH_URL;
    }
}
