package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.millenniumbcp;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MillenniumBcpAgent extends SibsBaseAgent {

    public MillenniumBcpAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    protected String getIntegrationName() {
        return MillenniumBcpConstants.INTEGRATION_NAME;
    }
}
