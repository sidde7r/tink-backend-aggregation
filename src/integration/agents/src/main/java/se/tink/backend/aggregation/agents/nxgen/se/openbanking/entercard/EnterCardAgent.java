package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.EnterCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.configuration.EnterCardConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.creditcardaccount.CreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.creditcardaccount.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class EnterCardAgent extends NextGenerationAgent {

    private final String clientName;
    private final EnterCardApiClient apiClient;

    public EnterCardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new EnterCardApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        apiClient.setConfiguration(getClientConfiguration());
    }

    protected EnterCardConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        EnterCardConstants.INTEGRATION_NAME,
                        clientName,
                        EnterCardConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new EnterCardAuthenticator(apiClient, persistentStorage, getClientConfiguration());
    }

    @Override
    public Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new CreditCardAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new CreditCardTransactionFetcher(apiClient)))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
