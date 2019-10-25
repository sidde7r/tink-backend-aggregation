package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveTiagoTestBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CaixaRedirectAgent extends SibsProgressiveTiagoTestBaseAgent {

    private static final String INTEGRATION_NAME = "caixa-redirect-pt";

    public CaixaRedirectAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    protected String getIntegrationName() {
        return INTEGRATION_NAME;
    }
}
