package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.BunqOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration.BunqConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.session.BunqSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseAgent;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class BunqAgent extends BunqBaseAgent {
    private final BunqApiClient apiClient;
    private final BunqClientAuthTokenHandler clientAuthTokenHandler;
    private String backendHost;
    private AgentConfiguration<BunqConfiguration> agentConfiguration;

    @Inject
    public BunqAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = new BunqApiClient(client, getBackendHost());
        this.clientAuthTokenHandler =
                new BunqClientAuthTokenHandler(persistentStorage, sessionStorage, temporaryStorage);
    }

    @Override
    protected String getBackendHost() {
        return Optional.ofNullable(backendHost)
                .orElseGet(
                        () -> {
                            backendHost = payload.split(" ")[1];
                            return backendHost;
                        });
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(BunqConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BunqOAuthAuthenticator(
                                apiClient,
                                clientAuthTokenHandler,
                                persistentStorage,
                                sessionStorage,
                                getAggregatorInfo().getAggregatorIdentifier(),
                                agentConfiguration),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BunqSessionHandler(apiClient, clientAuthTokenHandler, sessionStorage);
    }
}
