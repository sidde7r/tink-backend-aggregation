package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox;

import java.time.Duration;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.AhoiSandboxConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.AhoiSandboxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.configuration.AhoiSandboxConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.AhoiSandboxTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.AhoiSandboxTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AhoiSandboxAgent extends NextGenerationAgent {

    private final String clientName;
    private final AhoiSandboxApiClient apiClient;

    public AhoiSandboxAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);

        apiClient = new AhoiSandboxApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.setTimeout((int) Duration.ofSeconds(60).toMillis());
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        apiClient.setConfiguration(getClientConfiguration());
    }

    protected AhoiSandboxConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        AhoiSandboxConstants.INTEGRATION_NAME,
                        clientName,
                        AhoiSandboxConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new PasswordAuthenticationController(
                new AhoiSandboxAuthenticator(
                        apiClient, getClientConfiguration(), persistentStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final AhoiSandboxTransactionalAccountFetcher accountFetcher =
                new AhoiSandboxTransactionalAccountFetcher(apiClient);

        final AhoiSandboxTransactionalAccountTransactionFetcher transactionFetcher =
                new AhoiSandboxTransactionalAccountTransactionFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        transactionFetcher));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
