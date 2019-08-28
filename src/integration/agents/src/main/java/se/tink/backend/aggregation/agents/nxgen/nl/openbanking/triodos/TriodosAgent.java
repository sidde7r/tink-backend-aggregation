package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.TriodosAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.configuration.TriodosConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class TriodosAgent extends BerlinGroupAgent<TriodosApiClient, TriodosConfiguration> {
    private final TriodosApiClient apiClient;

    public TriodosAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new TriodosApiClient(client, sessionStorage);
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
    protected TriodosApiClient getApiClient() {
        apiClient.setCredentials(request.getCredentials());
        return apiClient;
    }

    @Override
    protected String getIntegrationName() {
        return TriodosConstants.INTEGRATION_NAME;
    }

    @Override
    protected Class<TriodosConfiguration> getConfigurationClassDescription() {
        return TriodosConfiguration.class;
    }
}
