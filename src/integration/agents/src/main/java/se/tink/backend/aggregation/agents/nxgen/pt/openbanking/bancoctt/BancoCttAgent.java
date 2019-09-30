package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.bancoctt;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoCttAgent extends SibsProgressiveBaseAgent {

    public BancoCttAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    protected String getIntegrationName() {
        return BancoCttConstants.INTEGRATION_NAME;
    }
}
