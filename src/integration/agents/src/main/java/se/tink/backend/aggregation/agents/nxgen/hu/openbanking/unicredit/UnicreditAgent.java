package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.UnicreditConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.UnicreditAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.UnicreditAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
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
        // TODO redirect back doesn't work on sandbox
        //      If works on prod custom controller not necessary
        final UnicreditAuthenticationController controller =
                new UnicreditAuthenticationController(
                        new UnicreditAuthenticator((UnicreditApiClient) apiClient),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }
}
