package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixacrl;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CaixaCrlAgent extends SibsBaseAgent {

    public CaixaCrlAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    protected String getIntegrationName() {
        return CaixaCrlConstants.INTEGRATION_NAME;
    }
}
