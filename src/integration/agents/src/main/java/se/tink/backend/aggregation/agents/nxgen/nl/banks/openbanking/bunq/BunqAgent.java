package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import java.util.Optional;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.BunqOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration.BunqConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.BunqPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseAgent;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class BunqAgent extends BunqBaseAgent {
    private final BunqApiClient apiClient;
    private String backendHost;
    private AgentConfiguration<BunqConfiguration> agentConfiguration;

    public BunqAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new BunqApiClient(client, getBackendHost());
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
                getAgentConfigurationController()
                        .getAgentCommonConfiguration(BunqConfiguration.class);
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
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        BunqPaymentExecutor bunqPaymentExecutor =
                new BunqPaymentExecutor(sessionStorage, apiClient, supplementalInformationHelper);
        return Optional.of(new PaymentController(bunqPaymentExecutor, bunqPaymentExecutor));
    }
}
