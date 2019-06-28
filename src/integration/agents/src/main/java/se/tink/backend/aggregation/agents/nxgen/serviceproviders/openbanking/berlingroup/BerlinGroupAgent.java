package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class BerlinGroupAgent<
                TApiCliient extends BerlinGroupApiClient,
                TConfiguration extends BerlinGroupConfiguration>
        extends NextGenerationAgent {
    private final String clientId;
    private final String clientName;

    public BerlinGroupAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        clientId = request.getProvider().getPayload();
        clientName = request.getProvider().getPayload();
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientId() {
        return clientId;
    }

    protected abstract TApiCliient getApiClient();

    protected void setupClient(final TinkHttpClient client) {}

    protected TConfiguration getConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        getIntegrationName(), getClientName(), getConfigurationClassDescription())
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        setupClient(client);

        //noinspection unchecked
        getApiClient().setConfiguration(getConfiguration());
    }

    protected abstract String getIntegrationName();

    protected abstract Class<TConfiguration> getConfigurationClassDescription();

    protected BerlinGroupAccountFetcher getAccountFetcher() {
        return new BerlinGroupAccountFetcher(getApiClient());
    }

    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new BerlinGroupTransactionFetcher(getApiClient());
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final BerlinGroupAccountFetcher accountFetcher = getAccountFetcher();
        final BerlinGroupTransactionFetcher transactionFetcher = getTransactionFetcher();

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(transactionFetcher))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
