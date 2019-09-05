package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.UnicreditConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.UnicreditAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UnicreditAgent extends UnicreditBaseAgent {

    public UnicreditAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return Market.INTEGRATION_NAME;
    }

    @Override
    protected UnicreditBaseApiClient getApiClient(boolean manualRequest) {
        return new UnicreditApiClient(client, persistentStorage, credentials, manualRequest);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new UnicreditAuthenticator((UnicreditApiClient) apiClient);
    }
}
