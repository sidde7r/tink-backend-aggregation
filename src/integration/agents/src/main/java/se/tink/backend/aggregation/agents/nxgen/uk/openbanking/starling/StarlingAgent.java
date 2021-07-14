package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigration;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.auth.StarlingOAuth2AuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.auth.StarlingOAuth2AuthorizationSpecification;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.StarlingTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.secrets.StarlingSecrets;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

/** Starling documentation is available at https://api-sandbox.starlingbank.com/api/swagger.yaml */
@AgentCapabilities({CHECKING_ACCOUNTS})
public final class StarlingAgent extends AgentPlatformAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                AgentPlatformAuthenticator,
                AgentPlatformStorageMigration {

    private final StarlingApiClient apiClient;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final AgentConfiguration<StarlingSecrets> agentConfiguration;

    @Inject
    public StarlingAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(StarlingSecrets.class);

        apiClient = new StarlingApiClient(client, persistentStorage);
        transferDestinationRefreshController = constructTransferDestinationRefreshController();
        transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource());
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new StarlingTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new StarlingTransactionFetcher(apiClient))
                                .setConsecutiveEmptyPagesLimit(3)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new StarlingTransferDestinationFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    public AgentAuthenticationProcess getAuthenticationProcess() {
        return new StarlingOAuth2AuthenticationConfig()
                .authenticationProcess(
                        new AgentPlatformHttpClient(client),
                        new StarlingOAuth2AuthorizationSpecification(agentConfiguration));
    }

    public boolean isBackgroundRefreshPossible() {
        return true;
    }

    @Override
    public AgentPlatformStorageMigrator getMigrator() {
        return new StarlingAgentPlatformStorageMigrator();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
        client.setEidasIdentity(
                new EidasIdentity(
                        context.getClusterId(),
                        context.getAppId(),
                        context.getCertId(),
                        context.getProviderId(),
                        getAgentClass()));
    }
}
