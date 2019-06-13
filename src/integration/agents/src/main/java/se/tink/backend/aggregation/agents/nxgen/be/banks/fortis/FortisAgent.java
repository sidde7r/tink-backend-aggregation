package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.FortisAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.FortisTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class FortisAgent extends NextGenerationAgent {
    private final FortisApiClient apiClient;

    public FortisAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);

        String[] payload = request.getProvider().getPayload().split(" ");
        String baseUrl = payload[0];
        String distributorId = payload[1];

        this.apiClient = new FortisApiClient(client, baseUrl, distributorId);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addMessageReader(new HtmlReader());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        FortisAuthenticator authenticator =
                new FortisAuthenticator(
                        catalog, persistentStorage, apiClient, supplementalInformationHelper);
        return new AutoAuthenticationController(
                request, systemUpdater, authenticator, authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        FortisTransactionalAccountFetcher accountFetcher =
                new FortisTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(accountFetcher, 1),
                                accountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new FortisSessionHandler(apiClient, persistentStorage);
    }
}
