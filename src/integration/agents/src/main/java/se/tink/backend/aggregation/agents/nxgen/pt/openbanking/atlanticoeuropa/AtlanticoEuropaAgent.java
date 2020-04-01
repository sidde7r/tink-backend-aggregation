package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.atlanticoeuropa;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AtlanticoEuropaAgent extends SibsProgressiveBaseAgent {

    private static final String INTEGRATION_NAME = "atlanticoeuropa";

    public AtlanticoEuropaAgent(
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
