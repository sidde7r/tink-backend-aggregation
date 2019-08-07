package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.BuddybankConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.BuddybankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.BuddybankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BuddybankAgent extends UnicreditBaseAgent {

    public BuddybankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return Market.INTEGRATION_NAME;
    }

    @Override
    protected UnicreditBaseApiClient getApiClient(boolean requestIsManual) {
        return new BuddybankApiClient(client, persistentStorage, credentials, requestIsManual);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BuddybankAuthenticationController(
                new BuddybankAuthenticator((BuddybankApiClient) apiClient));
    }
}
