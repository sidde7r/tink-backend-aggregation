package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CaixaRedirectAgent extends SibsProgressiveBaseAgent {

    public CaixaRedirectAgent(
            CredentialsRequest request, AgentContext context, AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    protected String getIntegrationName() {
        return CaixaConstants.INTEGRATION_REDIRECT_NAME;
    }
}
