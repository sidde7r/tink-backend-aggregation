package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.bancobpi;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoBpiAgent extends SibsBaseAgent {

    public BancoBpiAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    public String getIntegrationName() {
        return BancoBpiConstants.INTEGRATION_NAME;
    }
}
