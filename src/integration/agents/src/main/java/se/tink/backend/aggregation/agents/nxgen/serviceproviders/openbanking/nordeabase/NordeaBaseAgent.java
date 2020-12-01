package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public abstract class NordeaBaseAgent extends NextGenerationAgent {
    protected NordeaBaseApiClient apiClient;
    protected String language;

    public NordeaBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        try {
            this.language = request.getUser().getLocale().split("_")[0];
        } catch (RuntimeException e) {
            this.language = NordeaBaseConstants.QueryValues.DEFAULT_LANGUAGE;
        }
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(getAgentConfiguration());
        this.client.setEidasProxy(configuration.getEidasProxy());
        /* The agent is not supposed to follow the authorization redirect. The authorize redirect
        is between user (PSU) and the bank (ASPSP) handled in a third party app (browser) without
        TPP involvement */
        this.client.setFollowRedirects(false);
    }

    protected AgentConfiguration<NordeaBaseConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(NordeaBaseConfiguration.class);
    }

    protected String getProviderName() {
        return this.provider != null ? this.provider.getName() : null;
    }

    @Override
    protected abstract Authenticator constructAuthenticator();

    @Override
    protected abstract SessionHandler constructSessionHandler();
}
