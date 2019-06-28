package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.NordnetConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.NordnetConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.authenticator.NordnetAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.configuration.NordnetConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordnet.fetcher.transactionalaccount.NordnetTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NordnetAgent extends NextGenerationAgent {
    private final NordnetApiClient apiClient;

    public NordnetAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new NordnetApiClient(client, sessionStorage);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final NordnetConfiguration nordnetConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                Market.INTEGRATION_NAME,
                                request.getProvider().getPayload(),
                                NordnetConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_CONFIGURATION));

        apiClient.setConfiguration(nordnetConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new NordnetAuthenticator(apiClient, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        NordnetTransactionalAccountFetcher accountFetcher =
                new NordnetTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(accountFetcher))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
