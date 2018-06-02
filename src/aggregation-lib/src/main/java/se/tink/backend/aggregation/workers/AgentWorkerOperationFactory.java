package se.tink.backend.aggregation.workers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.CreateProductRequest;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.MigrateCredentialsDecryptRequest;
import se.tink.backend.aggregation.rpc.MigrateCredentialsReencryptRequest;
import se.tink.backend.aggregation.rpc.ProductInformationRequest;
import se.tink.backend.aggregation.rpc.ReencryptionRequest;
import se.tink.backend.aggregation.rpc.RefreshApplicationRequest;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.AgentWorkerOperation.AgentWorkerOperationState;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.CreateProductAgentWorkerCommand;
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
import se.tink.backend.aggregation.workers.commands.FetchProductInformationAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.KeepAliveAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LockAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.ProcessItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshApplicationAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.ReportProviderTransferMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SetCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.TransferAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ValidateProviderAgentWorkerStatus;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricCacheLoader;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.repository.mysql.aggregation.ClusterCryptoConfigurationRepository;
import se.tink.libraries.application.ApplicationType;
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

    // Explicit order of refreshable items.
    // Prioritize accounts before transactions in order to give faster feedback.
    private static final Ordering<RefreshableItem> REFRESHABLE_ITEM_ORDERING = Ordering.explicit(ImmutableList.of(
            RefreshableItem.CHECKING_ACCOUNTS,
            RefreshableItem.SAVING_ACCOUNTS,
            RefreshableItem.CREDITCARD_ACCOUNTS,
            RefreshableItem.LOAN_ACCOUNTS,
            RefreshableItem.INVESTMENT_ACCOUNTS,

            RefreshableItem.CHECKING_TRANSACTIONS,
            RefreshableItem.SAVING_TRANSACTIONS,
            RefreshableItem.CREDITCARD_TRANSACTIONS,
            RefreshableItem.LOAN_TRANSACTIONS,
            RefreshableItem.INVESTMENT_TRANSACTIONS,

            RefreshableItem.EINVOICES,
            RefreshableItem.TRANSFER_DESTINATIONS
    ));

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
        List<RefreshableItem> items = REFRESHABLE_ITEM_ORDERING.sortedCopy(itemsToRefresh);

        log.info("Items to refresh (sorted): {}", items.stream().map(Enum::name).collect(Collectors.joining(", ")));

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));

        for (RefreshableItem item : items) {
            commands.add(new RefreshItemAgentWorkerCommand(context, item, createMetricState(request)));
        }
        // Post refresh processing. Only once per data type (accounts, transactions etcetera)
        if (!Collections.disjoint(items, REFRESHABLE_ITEMS_ACCOUNTS)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.ACCOUNTS,
                    createMetricState(request)));
        }

        if (!Collections.disjoint(items, REFRESHABLE_ITEMS_TRANSACTIONS)) {
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

    public AgentWorkerOperation createCreateProductOperation(ClusterInfo clusterInfo, CreateProductRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context, useAggregationController,
                aggregationControllerAggregationClient, isAggregationCluster, clusterInfo));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, "create-product",
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
        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.AUTHENTICATING));

        if (Objects.equals(ApplicationType.OPEN_SAVINGS_ACCOUNT, request.getApplication().getType()) &&
                Objects.equals(request.getProvider().getName(), "collector-bankid")) {
            // Utilize login command for collector in order to get persistent login and error handling
            commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState,
                    createMetricState(request)));
        }

        commands.add(new CreateProductAgentWorkerCommand(context, request));
        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));

        // FIXME: Temporary hack to process the accounts after a new savings account has been established.
        if (Objects.equals(ApplicationType.OPEN_SAVINGS_ACCOUNT, request.getApplication().getType())) {

            if (Objects.equals(request.getProvider().getName(), "sbab-bankid")) {
                // Special treatment for SBAB
                commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.ACCOUNTS,
                        createMetricState(request)));
                commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.TRANSFER_DESTINATIONS,
                        createMetricState(request)));
            } else {
                // Normal case
                commands.add(new RefreshItemAgentWorkerCommand(context, RefreshableItem.ACCOUNTS,
                        createMetricState(request)));
                commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.ACCOUNTS,
                        createMetricState(request)));

                commands.add(new RefreshItemAgentWorkerCommand(context, RefreshableItem.TRANSFER_DESTINATIONS,
                        createMetricState(request)));
                commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.TRANSFER_DESTINATIONS,
                        createMetricState(request)));
            }
        }

        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATED));

        return new AgentWorkerOperation(agentWorkerOperationState, "create-product", request, commands, context);
    }

    public AgentWorkerOperation createRefreshApplicationOperation(ClusterInfo clusterInfo,
            RefreshApplicationRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        // FIXME: Do we need locks etc on this refresh call?
        List<AgentWorkerCommand> commands = Lists.newArrayList();
        commands.add(new ValidateProviderAgentWorkerStatus(context, useAggregationController,
                aggregationControllerAggregationClient, isAggregationCluster, clusterInfo));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, "refresh-application",
                reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new RefreshApplicationAgentWorkerCommand(context, request));

        return new AgentWorkerOperation(agentWorkerOperationState, "refresh-application", request, commands, context);
    }

    /**
     * Note: ProductInformationRequest inherits FakedCredentialsRequest, since this operation should not operate on
     * credential dependent information and no Credentials will be sent in to this method.
     *
     * So mind that when adding new commands to the Operation, that `Credentials` should not be used.
     */
    public AgentWorkerOperation createFetchProductInformationOperation(ClusterInfo clusterInfo,
            ProductInformationRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient, clusterInfo);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context, useAggregationController,
                aggregationControllerAggregationClient, isAggregationCluster, clusterInfo));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, "fetch-product-information",
                reportMetricsAgentWorkerCommandState));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new FetchProductInformationAgentWorkerCommand(context, request));

        return new AgentWorkerOperation(
                agentWorkerOperationState, "fetch-product-information", request, commands, context);
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
}
