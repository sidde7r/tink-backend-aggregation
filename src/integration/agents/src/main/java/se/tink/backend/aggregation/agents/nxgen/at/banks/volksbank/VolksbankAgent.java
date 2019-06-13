package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.VolksbankAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.authenticator.VolksbankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.VolksbankCheckingAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.fetcher.VolksbankCheckingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.session.VolksbankSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class VolksbankAgent extends NextGenerationAgent {

    private final VolksbankApiClient apiClient;

    public VolksbankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient = VolksbankApiClient.create(persistentStorage, sessionStorage, client);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(VolksbankConstants.USER_AGENT);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new VolksbankPasswordAuthenticator(apiClient),
                new VolksbankAutoAuthenticator(apiClient, persistentStorage, credentials));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new VolksbankCheckingAccountFetcher(apiClient, sessionStorage),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new VolksbankCheckingTransactionFetcher(apiClient)))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new VolksbankSessionHandler(apiClient);
    }
}
