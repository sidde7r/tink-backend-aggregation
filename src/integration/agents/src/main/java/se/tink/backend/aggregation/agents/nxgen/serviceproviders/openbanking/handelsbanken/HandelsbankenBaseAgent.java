package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken;

import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.configuration.HandelsbankenBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.HandelsbankenBaseCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.HandelsbankenBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.filter.HandelsbankenRejectedFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
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
    protected AgentConfiguration<HandelsbankenBaseConfiguration> agentConfiguration;
    protected HandelsbankenBaseConfiguration handelsbankenBaseConfiguration;
    protected TransactionalAccountRefreshController transactionalAccountRefreshController;
    protected CreditCardRefreshController creditCardRefreshController;

    public HandelsbankenBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        client.addFilter(new TimeoutFilter());
        client.addFilter(new HandelsbankenRejectedFilter());
        apiClient = constructApiClient();
    }

    public HandelsbankenBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        client.addFilter(new TimeoutFilter());
        client.addFilter(new HandelsbankenRejectedFilter());
        apiClient = constructApiClient();
    }

    protected abstract HandelsbankenBaseAccountConverter getAccountConverter();

    protected abstract LocalDate getMaxPeriodTransactions();

    protected abstract String getMarket();

    public HandelsbankenBaseApiClient constructApiClient() {
        return new HandelsbankenBaseApiClient(client, persistentStorage, getMarket());
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(HandelsbankenBaseConfiguration.class);
        handelsbankenBaseConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        apiClient.setConfiguration(agentConfiguration);
        this.client.setEidasProxy(configuration.getEidasProxy());

        this.client.addFilter(new BankServiceInternalErrorFilter());
        this.client.addFilter(new TimeoutFilter());

        if (handelsbankenBaseConfiguration.getClientId() != null) {
            context.getLogMasker()
                    .addAgentWhitelistedValues(
                            ImmutableSet.of(handelsbankenBaseConfiguration.getClientId()));
        }
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
                new HandelsbankenBaseTransactionalAccountFetcher(
                        apiClient, getMaxPeriodTransactions());

        accountFetcher.setConverter(getAccountConverter());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher).build()));
    }

    protected CreditCardRefreshController getCreditCardRefreshController() {
        final HandelsbankenBaseCreditCardFetcher creditCardFetcher =
                new HandelsbankenBaseCreditCardFetcher(apiClient, getMaxPeriodTransactions());

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(creditCardFetcher)
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
