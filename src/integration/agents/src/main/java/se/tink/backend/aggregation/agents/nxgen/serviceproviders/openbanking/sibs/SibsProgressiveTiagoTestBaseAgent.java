package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class SibsProgressiveTiagoTestBaseAgent extends SibsProgressiveBaseAgent {

    private static final String TIAGO_USER_ID = "ea47d3ed422840f9987d59b32a8d099e";

    public SibsProgressiveTiagoTestBaseAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    protected SibsConfiguration getClientConfiguration(Credentials credentials) {
        if (TIAGO_USER_ID.equals(credentials.getUserId())) {
            return getAgentConfigurationController().getAgentConfiguration(SibsConfiguration.class);
        }
        return configuration
                .getIntegrations()
                .getClientConfiguration(getIntegrationName(), clientName, SibsConfiguration.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SibsConstants.ErrorMessages.MISSING_CONFIGURATION));
    }
}
