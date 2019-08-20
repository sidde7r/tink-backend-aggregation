package se.tink.backend.aggregation.agents.nxgen.de.openbanking.norisbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NorisbankAgent extends DeutscheBankAgent {

    public NorisbankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration, NorisbankConstants.INTEGRATION_NAME);
    }
}
