package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator.UbiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationRedController;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UbiAgent extends CbiGlobeAgent {

    public UbiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return UbiConstants.INTEGRATION_NAME;
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new UbiApiClient(client, persistentStorage, requestManual);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final CbiGlobeAuthenticationRedController controller =
                new CbiGlobeAuthenticationRedController(
                        supplementalInformationHelper,
                        new UbiAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()),
                        new StrongAuthenticationState(request.getAppUriId()));

        return new AutoAuthenticationController(request, systemUpdater, controller, controller);
    }
}
