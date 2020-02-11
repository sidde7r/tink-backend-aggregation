package se.tink.backend.aggregation.agents.nxgen.be.openbanking.beobank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BeoBankAgent extends CmcicAgent {
    public BeoBankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
    }

    @Override
    protected String getIntegrationName() {
        return BeoBankConstants.INTEGRATION_NAME;
    }
}
