package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.BunqOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration.BunqConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.session.BunqSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BunqAgent extends BunqBaseAgent {
    private BunqConfiguration bunqConfiguration;
    private final BunqApiClient apiClient;

    public BunqAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new BunqApiClient(client, super.getAgentConfiguration().getBackendHost());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        bunqConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                BunqConstants.Market.INTEGRATION_NAME,
                                BunqConstants.Market.CLIENT_NAME,
                                BunqConfiguration.class)
                        .orElseThrow(
                                () -> new IllegalStateException("Bunq configuration missing."));

        BunqBaseConfiguration bunqBaseConfiguration = super.getAgentConfiguration();
        bunqConfiguration.setBackendHost(bunqBaseConfiguration.getBackendHost());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new BunqOAuthAuthenticator(
                                apiClient,
                                persistentStorage,
                                sessionStorage,
                                temporaryStorage,
                                getAggregatorInfo().getAggregatorIdentifier(),
                                bunqConfiguration));

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BunqSessionHandler();
    }
}
