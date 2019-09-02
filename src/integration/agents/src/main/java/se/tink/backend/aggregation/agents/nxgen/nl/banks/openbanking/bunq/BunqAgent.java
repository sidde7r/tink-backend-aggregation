package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.BunqOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration.BunqConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.BunqPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.session.BunqSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BunqAgent extends BunqBaseAgent {
    private final BunqApiClient apiClient;
    private final String clientName;
    private String backendHost;
    private BunqConfiguration bunqConfiguration;

    public BunqAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        clientName = payload.split(" ")[0];
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
        bunqConfiguration =
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
                                persistentStorage,
                                sessionStorage,
                                temporaryStorage,
                                getAggregatorInfo().getAggregatorIdentifier(),
                                bunqConfiguration),
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
        return new BunqSessionHandler();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(
                new PaymentController(
                        new BunqPaymentExecutor(
                                sessionStorage, apiClient, supplementalInformationHelper)));
    }
}
