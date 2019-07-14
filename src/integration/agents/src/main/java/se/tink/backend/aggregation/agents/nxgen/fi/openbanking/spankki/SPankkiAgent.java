package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SPankkiAgent extends CrosskeyBaseAgent {

    public SPankkiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    public String getIntegrationName() {
        return SPankkiConstants.Market.INTEGRATION_NAME;
    }

    @Override
    public String getClientName() {
        return SPankkiConstants.Market.CLIENT_NAME;
    }

    @Override
    protected String getBaseAPIUrl() {
        return SPankkiConstants.BASE_API_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return SPankkiConstants.BASE_AUTH_URL;
    }

    @Override
    protected String getxFapiFinancialId() {
        return SPankkiConstants.X_FAPI_FINANCIAL_ID;
    }
}
