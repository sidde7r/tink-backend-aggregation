package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.SebBalticsDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.configuration.SebBlaticsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.session.SebBalticsSessionHandler;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public abstract class SebBalticsBaseAgent<C extends SebBalticsBaseApiClient>
        extends SubsequentProgressiveGenerationAgent {

    protected C apiClient;
    protected AgentConfiguration<SebBlaticsConfiguration> agentConfiguration;
    protected SebBlaticsConfiguration sebConfiguration;
    protected OAuth2TokenStorage tokenStorage;

    protected SebBalticsBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = getApiClient();
        this.tokenStorage = new OAuth2TokenStorage(persistentStorage, sessionStorage);
    }

    protected abstract C getApiClient();

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(SebBlaticsConfiguration.class);
        sebConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        apiClient.setConfiguration(sebConfiguration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new SebBalticsDecoupledAuthenticator(
                apiClient, agentConfiguration, sessionStorage, persistentStorage, credentials);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SebBalticsSessionHandler(apiClient, persistentStorage);
    }
}
