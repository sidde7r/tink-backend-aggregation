package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.alandsbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsbankenAgent extends CrosskeyBaseAgent {

    public AlandsbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getIntegrationName() {
        return AlandsbankenConstants.Market.INTEGRATION_NAME;
    }

    @Override
    public String getClientName() {
        return AlandsbankenConstants.Market.CLIENT_NAME;
    }

    @Override
    protected String getBaseAPIUrl() {
        return AlandsbankenConstants.BASE_API_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return AlandsbankenConstants.BASE_AUTH_URL;
    }

    @Override
    protected String getxFapiFinancialId() {
        return AlandsbankenConstants.X_FAPI_FINANCIAL_ID;
    }
}
