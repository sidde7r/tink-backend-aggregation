package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.authenticatior.LaBanquePostaleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LaBanquePostaleAgent extends NextGenerationAgent {

    private final LaBanquePostaleApiClient apiClient;

    public LaBanquePostaleAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new LaBanquePostaleApiClient(client);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new LaBanquePostaleAuthenticator(apiClient);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        LaBanquePostaleTransactionalAccountFetcher fetcher =
                new LaBanquePostaleTransactionalAccountFetcher(apiClient);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        fetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(fetcher, 0))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LaBanquePostaleSessionHandler(apiClient);
    }
}
