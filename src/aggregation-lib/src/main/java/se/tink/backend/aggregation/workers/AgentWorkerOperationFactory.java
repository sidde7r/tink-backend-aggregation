package se.tink.backend.aggregation.workers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.MigrateCredentialsDecryptRequest;
import se.tink.backend.aggregation.rpc.MigrateCredentialsReencryptRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.ReencryptionRequest;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.AgentWorkerOperation.AgentWorkerOperationState;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.ClearSensitiveInformationCommand;
import se.tink.backend.aggregation.workers.commands.DebugAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DebugAgentWorkerCommand.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.DecryptAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DecryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DecryptForMigrationAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DeleteAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DeleteAgentWorkerCommand.DeleteAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.EncryptAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.EncryptAgentWorkerCommand.EncryptAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.EncryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SelectAccountsToBeUpdatedCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.KeepAliveAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LockAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.ProcessItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.ReportProviderTransferMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RequestUserOptInAccountsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountsToUpdateServiceAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SetCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.TransferAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ValidateProviderAgentWorkerStatus;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricCacheLoader;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.repository.mysql.aggregation.ClusterCryptoConfigurationRepository;
import se.tink.libraries.metrics.MetricRegistry;

public class AgentWorkerOperationFactory {
    private static final Logger log = LoggerFactory.getLogger(AgentWorkerOperationFactory.class);
    private InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState;
    private ServiceContext serviceContext;
    private EncryptAgentWorkerCommandState encryptAgentWorkerCommandState;
    private ReportProviderMetricsAgentWorkerCommandState reportMetricsAgentWorkerCommandState;
    private AgentWorkerOperationState agentWorkerOperationState;
    private CircuitBreakerAgentWorkerCommandState circuitBreakAgentWorkerCommandState;
    private DeleteAgentWorkerCommandState deleteAgentWorkerCommandState;
    private DebugAgentWorkerCommandState debugAgentWorkerCommandState;
    private LoginAgentWorkerCommandState loginAgentWorkerCommandState;
    private boolean useAggregationController;
    private AggregationControllerAggregationClient aggregationControllerAggregationClient;
    private final boolean isAggregationCluster;
    private final ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository;
    private final CacheClient cacheClient;

    private final MetricCacheLoader metricCacheLoader;

    private static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_ALL = ImmutableSet.<RefreshableItem>builder()
            .add(RefreshableItem.CHECKING_ACCOUNTS)
            .add(RefreshableItem.CHECKING_TRANSACTIONS)
            .add(RefreshableItem.SAVING_ACCOUNTS)
            .add(RefreshableItem.SAVING_TRANSACTIONS)
            .add(RefreshableItem.CREDITCARD_ACCOUNTS)
            .add(RefreshableItem.CREDITCARD_TRANSACTIONS)
            .add(RefreshableItem.LOAN_ACCOUNTS)
            .add(RefreshableItem.LOAN_TRANSACTIONS)
            .add(RefreshableItem.INVESTMENT_ACCOUNTS)
            .add(RefreshableItem.INVESTMENT_TRANSACTIONS)
            .add(RefreshableItem.EINVOICES)
            .add(RefreshableItem.TRANSFER_DESTINATIONS)
            .build();

    private static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_TRANSACTIONS = ImmutableSet.<RefreshableItem>builder()
            .add(RefreshableItem.CHECKING_TRANSACTIONS)
            .add(RefreshableItem.SAVING_TRANSACTIONS)
            .add(RefreshableItem.CREDITCARD_TRANSACTIONS)
            .add(RefreshableItem.LOAN_TRANSACTIONS)
            .add(RefreshableItem.INVESTMENT_TRANSACTIONS)
            .build();

    private static final ImmutableSet<RefreshableItem> REFRESHABLE_ITEMS_ACCOUNTS = ImmutableSet.<RefreshableItem>builder()
            .add(RefreshableItem.CHECKING_ACCOUNTS)
            .add(RefreshableItem.SAVING_ACCOUNTS)
            .add(RefreshableItem.CREDITCARD_ACCOUNTS)
            .add(RefreshableItem.LOAN_ACCOUNTS)
            .add(RefreshableItem.INVESTMENT_ACCOUNTS)
            .build();


    private MetricRegistry metricRegistry;

    public AgentWorkerOperationFactory(ServiceContext serviceContext, MetricRegistry metricRegistry,
            boolean useAggregationController, AggregationControllerAggregationClient aggregationControllerAggregationClient) {
        this.serviceContext = serviceContext;

        // Initialize agent worker command states.
        metricCacheLoader = new MetricCacheLoader(metricRegistry);

        instantiateAgentWorkerCommandState = new InstantiateAgentWorkerCommandState(serviceContext);
        encryptAgentWorkerCommandState = new EncryptAgentWorkerCommandState(serviceContext);
        reportMetricsAgentWorkerCommandState = new ReportProviderMetricsAgentWorkerCommandState(metricRegistry);
        circuitBreakAgentWorkerCommandState = new CircuitBreakerAgentWorkerCommandState(
                serviceContext.getConfiguration().getAggregationWorker().getCircuitBreaker(), metricRegistry);
        deleteAgentWorkerCommandState = new DeleteAgentWorkerCommandState(serviceContext);
        debugAgentWorkerCommandState = new DebugAgentWorkerCommandState(serviceContext);
        loginAgentWorkerCommandState = new LoginAgentWorkerCommandState(serviceContext, metricRegistry);
        agentWorkerOperationState = new AgentWorkerOperationState(metricRegistry);
        this.metricRegistry = metricRegistry;
        this.useAggregationController = useAggregationController;
        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;
        this.isAggregationCluster = serviceContext.isAggregationCluster();
        this.clusterCryptoConfigurationRepository =
                serviceContext.getRepository(ClusterCryptoConfigurationRepository.class);

        this.cacheClient = serviceContext.getCacheClient();
    }

    private AgentWorkerCommandMetricState createMetricState(CredentialsRequest request) {
        return new AgentWorkerCommandMetricState(request.getProvider(), request.getCredentials(), request.isManual(),
                metricCacheLoader, request.getType());
    }

    public AgentWorkerOperation createDeleteCredentialsOperation(ClusterInfo clusterInfo, CredentialsRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new DeleteAgentWorkerCommand(context, deleteAgentWorkerCommandState));

        return new AgentWorkerOperation(agentWorkerOperationState, "delete-credentials", request, commands,
                context);
    }

    // Remove `ACCOUNTS` and `TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS` and replace them with appropriate new
    // items.
    private Set<RefreshableItem> convertLegacyItems(Set<RefreshableItem> items) {
        if (items.contains(RefreshableItem.ACCOUNTS)) {
            items.remove(RefreshableItem.ACCOUNTS);
            items.addAll(REFRESHABLE_ITEMS_ACCOUNTS);
        }

        if (items.contains(RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS)) {
            items.remove(RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS);
            items.addAll(REFRESHABLE_ITEMS_TRANSACTIONS);
        }

        return items;
    }

    private List<AgentWorkerCommand> createRefreshableItemsChain(CredentialsRequest request, AgentWorkerContext context,
            Set<RefreshableItem> itemsToRefresh) {

        itemsToRefresh = convertLegacyItems(itemsToRefresh);

        // Sort the refreshable items
        List<RefreshableItem> items = RefreshableItem.sort(itemsToRefresh);

        log.info("Items to refresh (sorted): {}", items.stream().map(Enum::name).collect(Collectors.joining(", ")));

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));

        List<RefreshableItem> accountItems = items.stream()
                .filter(RefreshableItem::isAccount)
                .collect(Collectors.toList());

        List<RefreshableItem> nonAccountItems = items.stream()
                .filter(i -> !accountItems.contains(i))
                .collect(Collectors.toList());

        // if it is a follow up update, we do not refresh the accounts again
        if(!(request instanceof RefreshWhitelistInformationRequest
                && ((RefreshWhitelistInformationRequest) request).isOptIn())) {
            for (RefreshableItem item : accountItems) {
                commands.add(new RefreshItemAgentWorkerCommand(context, item, createMetricState(request)));
            }
        }

        if (accountItems.size() > 0) {
            commands.add(new SendAccountsToUpdateServiceAgentWorkerCommand(context, createMetricState(request)));
        }

        for (RefreshableItem item : nonAccountItems) {
            commands.add(new RefreshItemAgentWorkerCommand(context, item, createMetricState(request)));
        }

        // Post refresh processing. Only once per data type (accounts, transactions etcetera)
        if (RefreshableItem.hasAccounts(items)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.ACCOUNTS,
                    createMetricState(request)));
        }

        if (RefreshableItem.hasTransactions(items)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.TRANSACTIONS,
                    createMetricState(request)));
        }

        if (items.contains(RefreshableItem.EINVOICES)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.EINVOICES,
                    createMetricState(request)));
        }

        if (items.contains(RefreshableItem.TRANSFER_DESTINATIONS)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.TRANSFER_DESTINATIONS,
                    createMetricState(request)));
        }

        // Don't update the status if we are waiting on transactions from the connector.
        // Todo: Remove this dependency
        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATED,
                c -> !c.isWaitingOnConnectorTransactions()));

        return commands;
    }

    public AgentWorkerOperation createRefreshOperation(ClusterInfo clusterInfo, RefreshInformationRequest request) {

        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(REFRESHABLE_ITEMS_ALL);
        }

        log.debug("Creating refresh operation chain for credential");


        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = (request.isManual() ? "refresh-manual" : "refresh-auto");

        commands.add(new ValidateProviderAgentWorkerStatus(context, useAggregationController,
                aggregationControllerAggregationClient, isAggregationCluster, clusterInfo));
        commands.add(new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, metricsName,
                reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        if (isAggregationCluster) {
            commands.add(new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                    clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context));
        } else {
            commands.add(new DecryptAgentWorkerCommand(context, useAggregationController,
                    aggregationControllerAggregationClient));
        }
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)));

            commands.addAll(createRefreshAccountsCommandChain(request, context));
        if(request instanceof RefreshWhitelistInformationRequest && ((RefreshWhitelistInformationRequest) request).isOptIn()){
            RefreshWhitelistInformationRequest refreshWhiteList = (RefreshWhitelistInformationRequest) request;
            commands.add(new RequestUserOptInAccountsAgentWorkerCommand(context, refreshWhiteList));
        }

        commands.add(new SelectAccountsToBeUpdatedCommand(context, request));

        commands.addAll(createRefreshableItemsChain(request, context, request.getItemsToRefresh()));

        log.debug("Created refresh operation chain for credential");
        return new AgentWorkerOperation(agentWorkerOperationState, metricsName, request, commands, context);
    }

    public AgentWorkerOperation createExecuteTransferOperation(ClusterInfo clusterInfo, TransferRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context, useAggregationController,
                aggregationControllerAggregationClient, isAggregationCluster, clusterInfo));
        commands.add(new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, "execute-transfer",
                reportMetricsAgentWorkerCommandState));
        commands.add(new ReportProviderTransferMetricsAgentWorkerCommand(context,  "execute-transfer"));
        commands.add(new LockAgentWorkerCommand(context));
        if (isAggregationCluster) {
            commands.add(new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                    clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context));

        } else {
            commands.add(new DecryptAgentWorkerCommand(context, useAggregationController,
                    aggregationControllerAggregationClient));
        }

        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)));
        commands.add(new TransferAgentWorkerCommand(context, request, createMetricState(request)));

        // Refresh everything
        commands.addAll(createRefreshableItemsChain(request, context, REFRESHABLE_ITEMS_ALL));

        return new AgentWorkerOperation(agentWorkerOperationState, "execute-transfer", request, commands,
                context);
    }

    public AgentWorkerOperation createCreateCredentialsOperation(ClusterInfo clusterInfo, CredentialsRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));

        // acquire lock to avoid encryption/decryption race conditions
        commands.add(new LockAgentWorkerCommand(context));

        if (isAggregationCluster) {
            commands.add(new EncryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                    clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context, false));
        } else {
            commands.add(new EncryptAgentWorkerCommand(context, encryptAgentWorkerCommandState));
        }

        return new AgentWorkerOperation(agentWorkerOperationState, "create-credentials", request, commands,
                context);
    }

    public AgentWorkerOperation reencryptCredentialsOperation(ClusterInfo clusterInfo, ReencryptionRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();
        commands.add(new LockAgentWorkerCommand(context)); // Avoid refreshing while this is going on.
        commands.add(new DecryptAgentWorkerCommand(context, useAggregationController,
                aggregationControllerAggregationClient, false));
        commands.add(new EncryptAgentWorkerCommand(context, encryptAgentWorkerCommandState)
                .withOverwriteSecretKey(true));
        return new AgentWorkerOperation(agentWorkerOperationState, "reencrypt-credentials", request, commands,
                context);
    }

    public AgentWorkerOperation createUpdateOperation(ClusterInfo clusterInfo, CredentialsRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));

        // acquire lock to avoid encryption/decryption race conditions
        commands.add(new LockAgentWorkerCommand(context));

        if (isAggregationCluster) {
            commands.add(new EncryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                    clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context, false));
        } else {
            commands.add(new EncryptAgentWorkerCommand(context, encryptAgentWorkerCommandState));
        }

        return new AgentWorkerOperation(agentWorkerOperationState, "update-credentials", request, commands,
                context);
    }

    public AgentWorkerOperation createKeepAliveOperation(ClusterInfo clusterInfo, KeepAliveRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context, useAggregationController,
                aggregationControllerAggregationClient, isAggregationCluster, clusterInfo));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, "keep-alive",
                reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        if (isAggregationCluster) {
            commands.add(new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                    clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context));
        } else {
            commands.add(new DecryptAgentWorkerCommand(context, useAggregationController,
                    aggregationControllerAggregationClient));
        }
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new KeepAliveAgentWorkerCommand(context));

        return new AgentWorkerOperation(agentWorkerOperationState, "keep-alive", request, commands, context);
    }

    public AgentWorkerOperation createDecryptCredentialsOperation(ClusterInfo clusterInfo,
            MigrateCredentialsDecryptRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        ImmutableList<AgentWorkerCommand> commands = ImmutableList.of(
                new LockAgentWorkerCommand(context), // Avoid refreshing while this is going on.
                new DecryptForMigrationAgentWorkerCommand(context));

        return new AgentWorkerOperation(agentWorkerOperationState, "migrate-decrypt", request, commands, context);
    }

    public AgentWorkerOperation createReencryptCredentialsOperation(ClusterInfo clusterInfo,
            MigrateCredentialsReencryptRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        ImmutableList<AgentWorkerCommand> commands = ImmutableList.of(
                new LockAgentWorkerCommand(context), // Avoid refreshing while this is going on.
                new EncryptCredentialsWorkerCommand(clusterInfo, cacheClient, clusterCryptoConfigurationRepository,
                        aggregationControllerAggregationClient, context));

        return new AgentWorkerOperation(agentWorkerOperationState, "migrate-reencrypt", request, commands, context);
    }

    // for each account type,
    private List<AgentWorkerCommand> createRefreshAccountsCommandChain(RefreshInformationRequest request,
            AgentWorkerContext context) {

        Set<RefreshableItem> itemsToRefresh = convertLegacyItems(request.getItemsToRefresh());

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        for (RefreshableItem item : itemsToRefresh) {
            if (RefreshableItem.isAccount(item)) {
                commands.add(new RefreshItemAgentWorkerCommand(context, item, createMetricState(request)));
            }
        }

        return commands;
    }

    /**
     *  the endpoint opt-in supports the initial refresh with opt-in, aka, display all accounts for user to select the
     *  accounts to aggregate to. it also supports refreshing and updating only white-flagged accounts and the data.
     *  when the opt-in flag is true, it indicates that we need user to select accounts again, therefore we fetch
     *  only account data. after fetching, we set the flag into false, and put all white listed accounts in refresh.
     *  when the opt-in flag is false, we refresh and update all white listed accounts
     **/
    public AgentWorkerOperation createOptInRefreshOperation(ClusterInfo clusterInfo,
            RefreshWhitelistInformationRequest request) {
        return createRefreshOperation(clusterInfo, request);
    }
}
