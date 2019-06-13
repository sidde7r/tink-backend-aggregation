package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.BunqTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.BunqTransactionalTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.filter.BunqRequiredHeadersFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.filter.BunqSignatureHeaderFilter;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class BunqBaseAgent extends NextGenerationAgent {

    protected final String payload;
    private final BunqBaseApiClient apiClient;
    protected TemporaryStorage temporaryStorage;

    public BunqBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        payload = Preconditions.checkNotNull(request.getProvider().getPayload());
        this.apiClient = new BunqBaseApiClient(client, getBackendHost());
        temporaryStorage = new TemporaryStorage();
        configureHttpClient(client);
    }

    protected abstract String getBackendHost();

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BunqRequiredHeadersFilter(sessionStorage));
        client.addFilter(
                new BunqSignatureHeaderFilter(
                        sessionStorage, temporaryStorage, client.getUserAgent()));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new BunqTransactionalAccountFetcher(sessionStorage, apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        new BunqTransactionalTransactionsFetcher(
                                                sessionStorage, apiClient)))));
    }
}
