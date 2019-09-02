package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticationController;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoPostaAgent extends CbiGlobeAgent {

    public BancoPostaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new BancoPostaApiClient(client, persistentStorage, request.isManual());
    }

    @Override
    protected String getIntegrationName() {
        return BancoPostaConstants.INTEGRATION_NAME;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        controller =
                new CbiGlobeAuthenticationController(
                        supplementalInformationHelper,
                        new BancoPostaAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()),
                        new StrongAuthenticationState(request.getAppUriId()));

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }
}
