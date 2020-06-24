package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.TriodosAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.configuration.TriodosConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;

public final class TriodosAgent extends BerlinGroupAgent<TriodosApiClient, TriodosConfiguration> {

    @Inject
    public TriodosAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected TriodosApiClient createApiClient() {
        return new TriodosApiClient(
                client, sessionStorage, getConfiguration(), this.request.getCredentials());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new TriodosAuthenticator(apiClient),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<TriodosConfiguration> getConfigurationClassDescription() {
        return TriodosConfiguration.class;
    }
}
