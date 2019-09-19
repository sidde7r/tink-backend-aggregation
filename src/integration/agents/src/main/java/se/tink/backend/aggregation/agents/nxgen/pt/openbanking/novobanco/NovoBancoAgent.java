package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.novobanco;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NovoBancoAgent extends SibsBaseAgent {

    public NovoBancoAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    protected String getIntegrationName() {
        return NovoBancoConstants.INTEGRATION_NAME;
    }
}
