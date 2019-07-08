package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpsondrio;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpsondrio.authenticator.BPSondrioAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationController;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BPSondrioAgent extends CbiGlobeAgent {

    public BPSondrioAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return BPSondrioConstants.INTEGRATION_NAME;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        controller =
                new CbiGlobeAuthenticationController(
                        supplementalInformationHelper,
                        new BPSondrioAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()));

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }
}
