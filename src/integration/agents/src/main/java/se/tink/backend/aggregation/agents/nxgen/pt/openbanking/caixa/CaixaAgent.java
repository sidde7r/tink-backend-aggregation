package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa.CaixaConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsDecoupledAuthenticationController;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CaixaAgent extends SibsBaseAgent {

    public CaixaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return Market.INTEGRATION_NAME;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new SibsDecoupledAuthenticationController(new SibsAuthenticator(apiClient));
    }
}
