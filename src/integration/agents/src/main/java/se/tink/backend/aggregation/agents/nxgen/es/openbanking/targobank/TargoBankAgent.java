package se.tink.backend.aggregation.agents.nxgen.es.openbanking.targobank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class TargoBankAgent extends CmcicAgent {
    public TargoBankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
    }

    @Override
    protected String getIntegrationName() {
        return TargobankConstants.INTEGRATION_NAME;
    }
}
