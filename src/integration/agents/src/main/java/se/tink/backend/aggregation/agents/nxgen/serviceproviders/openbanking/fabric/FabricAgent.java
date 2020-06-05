package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.FabricAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.FabricRedirectAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.configuration.FabricConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.FabricAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.FabricTransactionFetcher;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;

public class FabricAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected final String clientName;
    protected final FabricApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public FabricAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        apiClient =
                new FabricApiClient(
                        client, persistentStorage, componentProvider.getRandomValueGenerator());
        clientName = request.getProvider().getPayload();
        transactionalAccountRefreshController =
                getTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource());
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        apiClient.setConfiguration(getClientConfiguration());
        client.setEidasProxy(configuration.getEidasProxy());
    }

    protected FabricConfiguration getClientConfiguration() {
        FabricConfiguration fabricConfiguration;
        try {
            fabricConfiguration =
                    getAgentConfigurationController()
                            .getAgentConfiguration(FabricConfiguration.class);
        } catch (IllegalStateException e) {
            throw new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION);
        }
        return fabricConfiguration;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final FabricRedirectAuthenticationController controller =
                new FabricRedirectAuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new FabricAuthenticator(apiClient, persistentStorage, credentials),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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

    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        final FabricAccountFetcher accountFetcher = new FabricAccountFetcher(apiClient);

        final FabricTransactionFetcher transactionFetcher = new FabricTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                transactionFetcher, 4, 85, ChronoUnit.DAYS, localDateTimeSource)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
