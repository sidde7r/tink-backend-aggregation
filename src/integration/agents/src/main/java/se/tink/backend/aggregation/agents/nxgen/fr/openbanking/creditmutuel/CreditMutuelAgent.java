package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditmutuel;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditMutuelAgent extends CmcicAgent {
    public CreditMutuelAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
    }

    @Override
    protected String getIntegrationName() {
        return CreditMutuelConstants.INTEGRATION_NAME;
    }
}
