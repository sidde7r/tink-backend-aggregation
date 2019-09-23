package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.santander;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SantanderAgent extends SibsProgressiveBaseAgent {

    public SantanderAgent(
            CredentialsRequest request, AgentContext context, AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    public String getIntegrationName() {
        return SantanderConstants.INTEGRATION_NAME;
    }
}
