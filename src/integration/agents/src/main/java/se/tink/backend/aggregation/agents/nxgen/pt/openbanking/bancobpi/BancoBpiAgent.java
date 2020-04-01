package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.bancobpi;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoBpiAgent extends SibsProgressiveBaseAgent {

    private static final String INTEGRATION_NAME = "bancobpi-pt";

    public BancoBpiAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    public String getIntegrationName() {
        return INTEGRATION_NAME;
    }
}
