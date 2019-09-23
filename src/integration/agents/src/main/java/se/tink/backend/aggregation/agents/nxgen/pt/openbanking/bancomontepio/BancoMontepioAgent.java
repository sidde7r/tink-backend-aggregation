package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.bancomontepio;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoMontepioAgent extends SibsProgressiveBaseAgent {

    public BancoMontepioAgent(
            CredentialsRequest request, AgentContext context, AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    protected String getIntegrationName() {
        return BancoMontepioConstants.INTEGRATION_NAME;
    }
}
