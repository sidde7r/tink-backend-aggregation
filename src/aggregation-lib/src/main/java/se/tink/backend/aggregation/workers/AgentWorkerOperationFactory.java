package se.tink.backend.aggregation.workers;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentFactory;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.api.WhitelistedTransferRequest;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.ReEncryptCredentialsRequest;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.storage.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.AgentWorkerOperation.AgentWorkerOperationState;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ClearSensitiveInformationCommand;
import se.tink.backend.aggregation.workers.commands.DebugAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DecryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.EncryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.KeepAliveAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LockAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ProcessItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderTransferMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RequestUserOptInAccountsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SelectAccountsToAggregateCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountsToUpdateServiceAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SetCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.TransferAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ValidateProviderAgentWorkerStatus;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricCacheLoader;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.backend.aggregation.legacy.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.aggregation.configurations.repositories.ClusterCryptoConfigurationRepository;
import se.tink.libraries.metrics.MetricRegistry;

public class AgentWorkerOperationFactory {
    private static final Logger log = LoggerFactory.getLogger(AgentWorkerOperationFactory.class);

    private final ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository;
    private final CacheClient cacheClient;
    private final MetricCacheLoader metricCacheLoader;

    // States
    private AgentWorkerOperationState agentWorkerOperationState;
    private CircuitBreakerAgentWorkerCommandState circuitBreakAgentWorkerCommandState;
    private DebugAgentWorkerCommandState debugAgentWorkerCommandState;
    private InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState;
    private LoginAgentWorkerCommandState loginAgentWorkerCommandState;
    private ReportProviderMetricsAgentWorkerCommandState reportMetricsAgentWorkerCommandState;

    private AggregationControllerAggregationClient aggregationControllerAggregationClient;
    private MetricRegistry metricRegistry;
    private ServiceContext serviceContext;
    private final AgentDebugStorageHandler agentDebugStorageHandler;

    @Inject
    public AgentWorkerOperationFactory(ServiceContext serviceContext, CacheClient cacheClient,
            MetricRegistry metricRegistry,
            AggregationControllerAggregationClient aggregationControllerAggregationClient,
            AgentDebugStorageHandler agentDebugStorageHandler, AgentWorkerOperationState agentWorkerOperationState,
            DebugAgentWorkerCommandState debugAgentWorkerCommandState,
            CircuitBreakerAgentWorkerCommandState circuitBreakerAgentWorkerCommandState,
            InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState,
            LoginAgentWorkerCommandState loginAgentWorkerCommandState,
            ReportProviderMetricsAgentWorkerCommandState reportProviderMetricsAgentWorkerCommandState) {
        this.clusterCryptoConfigurationRepository =
                serviceContext.getRepository(ClusterCryptoConfigurationRepository.class);
        this.cacheClient = cacheClient;
        metricCacheLoader = new MetricCacheLoader(metricRegistry);

        // Initialize agent worker command states.
        this.agentWorkerOperationState = agentWorkerOperationState;
        this.debugAgentWorkerCommandState = debugAgentWorkerCommandState;
        circuitBreakAgentWorkerCommandState = circuitBreakerAgentWorkerCommandState;
        this.instantiateAgentWorkerCommandState = instantiateAgentWorkerCommandState;
        this.loginAgentWorkerCommandState = loginAgentWorkerCommandState;
        this.reportMetricsAgentWorkerCommandState = reportProviderMetricsAgentWorkerCommandState;

        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;
        this.metricRegistry = metricRegistry;
        this.serviceContext = serviceContext;
        this.agentDebugStorageHandler = agentDebugStorageHandler;
    }

    private AgentWorkerCommandMetricState createMetricState(CredentialsRequest request) {
        return new AgentWorkerCommandMetricState(request.getProvider(), request.getCredentials(), metricCacheLoader,
                request.getType());
    }

    // Remove `ACCOUNTS` and `TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS` and replace them with appropriate new
    // items.
    private Set<RefreshableItem> convertLegacyItems(Set<RefreshableItem> items) {
        if (items.contains(RefreshableItem.ACCOUNTS)) {
            items.remove(RefreshableItem.ACCOUNTS);
            items.addAll(RefreshableItem.REFRESHABLE_ITEMS_ACCOUNTS);
        }

        if (items.contains(RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS)) {
            items.remove(RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS);
            items.addAll(RefreshableItem.REFRESHABLE_ITEMS_TRANSACTIONS);
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

        if (accountItems.size() > 0) {
            commands.add(new SendAccountsToUpdateServiceAgentWorkerCommand(context, createMetricState(request)));
        }

        for (RefreshableItem item : nonAccountItems) {
            commands.add(new RefreshItemAgentWorkerCommand(context, item, createMetricState(request)));
        }

        // FIXME: remove when Handelsbanken and Avanza have been moved to the nextgen agents. (TOP PRIO)
        // Due to the agents depending on updateTransactions to populate the the Accounts list
        // We need to reselect and send accounts to system
        if (!isNextGenerationAgent(request.getProvider())) {
            commands.add(new SelectAccountsToAggregateCommand(context, request));
            commands.add(new SendAccountsToUpdateServiceAgentWorkerCommand(context, createMetricState(request)));
        }

        // Post refresh processing. Only once per data type (accounts, transactions etcetera)
        if (RefreshableItem.hasAccounts(items)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.ACCOUNTS,
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

        // Transactions are processed last of the refreshable items since the credential status will be set `UPDATED`
        // by system when the processing is done.
        if (RefreshableItem.hasTransactions(items)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.TRANSACTIONS,
                    createMetricState(request)));
        }

        // Update the status to `UPDATED` if the credential isn't waiting on transactions from the connector and if
        // transactions aren't processed in system. The transaction processing in system will set the status to
        // `UPDATED` when transactions have been processed and new statistics are generated.
        // Todo: Remove this dependency
        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATED,
                c -> !c.isWaitingOnConnectorTransactions() && !c.isSystemProcessingTransactions()));

        return commands;
    }

    public AgentWorkerOperation createRefreshOperation(ClusterInfo clusterInfo, RefreshInformationRequest request) {
        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        log.debug("Creating refresh operation chain for credential");


        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = (request.isManual() ? "refresh-manual" : "refresh-auto");

        commands.add(new ValidateProviderAgentWorkerStatus(context,
                aggregationControllerAggregationClient, clusterInfo));
        commands.add(new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, metricsName,
                reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context));
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)));

        commands.addAll(createRefreshAccountsCommandChain(request, context, request.getItemsToRefresh()));
        commands.add(new SelectAccountsToAggregateCommand(context, request));

        commands.addAll(createRefreshableItemsChain(request, context, request.getItemsToRefresh()));

        log.debug("Created refresh operation chain for credential");
        return new AgentWorkerOperation(agentWorkerOperationState, metricsName, request, commands, context);
    }

    public AgentWorkerOperation createExecuteTransferOperation(ClusterInfo clusterInfo, TransferRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        String operationName = "execute-transfer";

        List<AgentWorkerCommand> commands = createTransferBaseCommands(clusterInfo, request, context, operationName);
        commands.addAll(createRefreshAccountsCommandChain(request, context, RefreshableItem.REFRESHABLE_ITEMS_ALL));
        commands.add(new SelectAccountsToAggregateCommand(context, request));
        // Refresh everything
        commands.addAll(createRefreshableItemsChain(request, context, RefreshableItem.REFRESHABLE_ITEMS_ALL));

        return new AgentWorkerOperation(agentWorkerOperationState, operationName, request, commands,
                context);
    }

    public AgentWorkerOperation createExecuteWhitelistedTransferOperation(ClusterInfo clusterInfo,
            WhitelistedTransferRequest request) {

        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        context.setWhitelistRefresh(true);

        String operationName = "execute-whitelisted-transfer";

        List<AgentWorkerCommand> commands = createTransferBaseCommands(clusterInfo, request, context, operationName);
        commands.addAll(
                createWhitelistRefreshableItemsChain(request, context, clusterInfo,
                        RefreshableItem.REFRESHABLE_ITEMS_ALL));

        return new AgentWorkerOperation(agentWorkerOperationState, operationName, request, commands,
                context);
    }

    private List<AgentWorkerCommand> createTransferBaseCommands(ClusterInfo clusterInfo, TransferRequest request,
            AgentWorkerContext context, String operationName) {

        return Lists.newArrayList(
                new ValidateProviderAgentWorkerStatus(context, aggregationControllerAggregationClient, clusterInfo),
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState),
                new ReportProviderMetricsAgentWorkerCommand(context, operationName,
                        reportMetricsAgentWorkerCommandState),
                new ReportProviderTransferMetricsAgentWorkerCommand(context,  operationName),
                new LockAgentWorkerCommand(context),
                new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                        clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context),
                new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler),
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState),
                new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)),
                new TransferAgentWorkerCommand(context, request, createMetricState(request)));
    }

    public AgentWorkerOperation createCreateCredentialsOperation(ClusterInfo clusterInfo, CredentialsRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));

        // acquire lock to avoid encryption/decryption race conditions
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new EncryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context, false));

        return new AgentWorkerOperation(agentWorkerOperationState, "create-credentials", request, commands,
                context);
    }

    public AgentWorkerOperation createUpdateOperation(ClusterInfo clusterInfo, CredentialsRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));

        // acquire lock to avoid encryption/decryption race conditions
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new EncryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context, false));

        return new AgentWorkerOperation(agentWorkerOperationState, "update-credentials", request, commands,
                context);
    }

    public AgentWorkerOperation createKeepAliveOperation(ClusterInfo clusterInfo, KeepAliveRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context,
                aggregationControllerAggregationClient, clusterInfo));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, "keep-alive",
                reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context));
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new KeepAliveAgentWorkerCommand(context));

        return new AgentWorkerOperation(agentWorkerOperationState, "keep-alive", request, commands, context);
    }

    public AgentWorkerOperation createReEncryptCredentialsOperation(ClusterInfo clusterInfo,
            ReEncryptCredentialsRequest request) {
        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        ImmutableList<AgentWorkerCommand> commands = ImmutableList.of(
                new LockAgentWorkerCommand(context),
                new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient, clusterCryptoConfigurationRepository,
                        aggregationControllerAggregationClient, context),
                new EncryptCredentialsWorkerCommand(clusterInfo, cacheClient, clusterCryptoConfigurationRepository,
                        aggregationControllerAggregationClient, context)
        );

        return new AgentWorkerOperation(agentWorkerOperationState, "reencrypt-credentials", request,
                commands, context);
    }

    // for each account type,
    private List<AgentWorkerCommand> createRefreshAccountsCommandChain(CredentialsRequest request,
            AgentWorkerContext context, Set<RefreshableItem> itemsToRefresh) {

        List<RefreshableItem> items = RefreshableItem.sort(convertLegacyItems(itemsToRefresh));

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        for (RefreshableItem item : items) {
            if (RefreshableItem.isAccount(item)) {
                commands.add(new RefreshItemAgentWorkerCommand(context, item, createMetricState(request)));
            }
        }

        return commands;
    }

    /**
     *
     * Use this operation when refreshing only the accounts that are available in the request.
     *
     **/
    public AgentWorkerOperation createWhitelistRefreshOperation(ClusterInfo clusterInfo,
            RefreshWhitelistInformationRequest request) {
        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        log.debug("Creating whitelist refresh operation chain for credential");


        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        context.setWhitelistRefresh(true);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = (request.isManual() ? "refresh-manual" : "refresh-auto");

        commands.add(new ValidateProviderAgentWorkerStatus(context,
                aggregationControllerAggregationClient, clusterInfo));
        commands.add(new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, metricsName,
                reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context));
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)));
        commands.addAll(
                createWhitelistRefreshableItemsChain(request, context, clusterInfo, request.getItemsToRefresh()));

        log.debug("Created whitelist refresh operation chain for credential");
        return new AgentWorkerOperation(agentWorkerOperationState, metricsName, request, commands, context);
    }

    /**
     *
     * Use this operation when changing whitelisted accounts and then doing a refresh.
     *
     */
    public AgentWorkerOperation createConfigureWhitelistOperation(ClusterInfo clusterInfo,
            ConfigureWhitelistInformationRequest request) {
        String operationMetricName = "configure-whitelist";

        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        AgentWorkerContext context = new AgentWorkerContext(request, metricRegistry,
                aggregationControllerAggregationClient, clusterInfo, serviceContext.getCoordinationClient(),
                serviceContext.getCacheClient(), serviceContext.getConfiguration().getAgentsServiceConfiguration());

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context,
                aggregationControllerAggregationClient, clusterInfo));
        commands.add(new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, operationMetricName,
                reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new DecryptCredentialsWorkerCommand(clusterInfo, cacheClient,
                    clusterCryptoConfigurationRepository, aggregationControllerAggregationClient, context));

        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)));
        commands.addAll(
                createWhitelistRefreshableItemsChain(request, context, clusterInfo, request.getItemsToRefresh()));

        return new AgentWorkerOperation(agentWorkerOperationState, operationMetricName, request,
                commands, context);
    }

    private ImmutableList<AgentWorkerCommand> createWhitelistRefreshableItemsChain(CredentialsRequest request,
            AgentWorkerContext context, ClusterInfo clusterInfo,
            Set<RefreshableItem> itemsToRefresh) {

        // Convert legacy items to corresponding new refreshable items
        itemsToRefresh = convertLegacyItems(itemsToRefresh);

        // Sort the refreshable items
        List<RefreshableItem> items = RefreshableItem.sort(itemsToRefresh);
        log.info("Items to refresh (sorted): {}", items.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ")));

        ImmutableList.Builder<AgentWorkerCommand> commands = ImmutableList.builder();

        // Update credentials status to updating to inform systems that credentials is being updated.
        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));

        Set<RefreshableItem> accountItems = items.stream()
                .filter(RefreshableItem::isAccount)
                .collect(Collectors.toSet());

        // === START REFRESHING ===
        if (accountItems.size() > 0) {
            // Start refreshing all account items
            commands.addAll(createRefreshAccountsCommandChain(request, context, accountItems));

            // If this is an optIn request we request the caller do supply supplemental information with the
            // accounts they want to whitelist.
            if (request instanceof ConfigureWhitelistInformationRequest) {
                commands.add(new RequestUserOptInAccountsAgentWorkerCommand(context,
                        (ConfigureWhitelistInformationRequest) request, clusterInfo,
                        aggregationControllerAggregationClient));
            }

            // Update the accounts on system side
            commands.add(new SelectAccountsToAggregateCommand(context, request));
            commands.add(new SendAccountsToUpdateServiceAgentWorkerCommand(context, createMetricState(request)));
        }

        // Add all refreshable items that aren't accounts to refresh them.
        items.stream()
                .filter(i -> !accountItems.contains(i))
                .forEach(item ->
                        commands.add(new RefreshItemAgentWorkerCommand(context, item, createMetricState(request))));
        // === END REFRESHING ===

        // === START PROCESSING ===
        // Post refresh processing. Only once per data type (accounts, transactions etc.).
        if (RefreshableItem.hasAccounts(items)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.ACCOUNTS,
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

        // Transactions are processed last of the refreshable items since the credential status will be set `UPDATED`
        // by system when the processing is done.
        if (RefreshableItem.hasTransactions(items)) {
            commands.add(new ProcessItemAgentWorkerCommand(context, ProcessableItem.TRANSACTIONS,
                    createMetricState(request)));
        }
        // === END PROCESSING ===

        // Update the status to `UPDATED` if the credential isn't waiting on transactions from the connector and if
        // transactions aren't processed in system. The transaction processing in system will set the status to
        // `UPDATED` when transactions have been processed and new statistics are generated.
        // Todo: Remove this dependency
        commands.add(new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATED,
                c -> !c.isWaitingOnConnectorTransactions() && !c.isSystemProcessingTransactions()));

        return commands.build();
    }

    /**
     *
     * Helper method giving info if this is a Provider with a Next Generation Agent.
     *
     * @return a boolean telling if this provider points to a Next Generation Agent.
     */
    private static boolean isNextGenerationAgent(Provider provider) {
        if (provider == null) {
            return false;
        }

        if (Strings.isNullOrEmpty(provider.getClassName())) {
            return false;
        }

        try {
            Class<? extends Agent> agentClass = AgentFactory.getAgentClass(provider);
            return NextGenerationAgent.class.isAssignableFrom(agentClass);
        } catch (Exception e) {
            return false;
        }
    }
}
