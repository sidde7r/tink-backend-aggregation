package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.mollerupsparkasse;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.mollerupsparkasse.MollerupSparkasseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.BecAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MollerupSparkasseAgent extends BecAgent {

    public MollerupSparkasseAgent(
            CredentialsRequest request, AgentContext context, AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
    }

    @Override
    protected String getBaseUrl() {
        return Urls.BASE_URL;
    }
}
