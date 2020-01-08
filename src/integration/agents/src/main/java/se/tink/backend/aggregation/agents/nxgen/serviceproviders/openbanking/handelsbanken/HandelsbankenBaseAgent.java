package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import java.util.Date;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.HandelsbankenBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.filter.HandelsbankenRejectedFilter;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class HandelsbankenBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected final HandelsbankenBaseApiClient apiClient;
    private HandelsbankenBaseConfiguration handelsbankenBaseConfiguration;
    protected TransactionalAccountRefreshController transactionalAccountRefreshController;

    public HandelsbankenBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        client.addFilter(new TimeoutFilter());
        client.addFilter(new HandelsbankenRejectedFilter());
        apiClient = new HandelsbankenBaseApiClient(client, persistentStorage, getMarket());

        setMaxPeriodTransactions();
    }

    protected abstract HandelsbankenBaseAccountConverter getAccountConverter();

    protected abstract Date setMaxPeriodTransactions();

    protected abstract String getMarket();

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        handelsbankenBaseConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(HandelsbankenBaseConfiguration.class);

        apiClient.setConfiguration(handelsbankenBaseConfiguration);
        this.client.setEidasProxy(configuration.getEidasProxy());

        this.client.addFilter(new BankServiceInternalErrorFilter());
        this.client.addFilter(new TimeoutFilter());
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final HandelsbankenBaseTransactionalAccountFetcher accountFetcher =
                new HandelsbankenBaseTransactionalAccountFetcher(apiClient, sessionStorage);

        accountFetcher.setConverter(getAccountConverter());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(accountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
