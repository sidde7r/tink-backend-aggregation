package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancacarige;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancacarige.authenticator.BancaCarigeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationRedController;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancaCarigeAgent extends CbiGlobeAgent {
    public BancaCarigeAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return BancaCarigeConstants.INTEGRATION_NAME;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final CbiGlobeAuthenticationRedController controller =
                new CbiGlobeAuthenticationRedController(
                        supplementalInformationHelper,
                        new BancaCarigeAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()),
                        new StrongAuthenticationState(request.getAppUriId()));

        return new AutoAuthenticationController(request, systemUpdater, controller, controller);
    }
}
