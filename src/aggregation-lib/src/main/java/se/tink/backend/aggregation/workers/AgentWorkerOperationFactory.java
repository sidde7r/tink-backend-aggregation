package se.tink.backend.aggregation.workers;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentClassFactory;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.api.WhitelistedTransferRequest;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
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
import se.tink.backend.aggregation.storage.database.providers.AggregatorInfoProvider;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
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
import se.tink.backend.aggregation.workers.commands.SetCredentialsStatusToAuthenticatingAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.TransferAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ValidateProviderAgentWorkerStatus;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricCacheLoader;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.metrics.MetricRegistry;

public class AgentWorkerOperationFactory {
    private static final Logger log = LoggerFactory.getLogger(AgentWorkerOperationFactory.class);

    private final CacheClient cacheClient;
    private final MetricCacheLoader metricCacheLoader;
    private final CryptoConfigurationDao cryptoConfigurationDao;
    private final ControllerWrapperProvider controllerWrapperProvider;
    private final AggregatorInfoProvider aggregatorInfoProvider;
    private final CuratorFramework coordinationClient;
    private final AgentsServiceConfiguration agentsServiceConfiguration;

    // States
    private AgentWorkerOperationState agentWorkerOperationState;
    private CircuitBreakerAgentWorkerCommandState circuitBreakAgentWorkerCommandState;
    private DebugAgentWorkerCommandState debugAgentWorkerCommandState;
    private InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState;
    private LoginAgentWorkerCommandState loginAgentWorkerCommandState;
    private ReportProviderMetricsAgentWorkerCommandState reportMetricsAgentWorkerCommandState;

    private MetricRegistry metricRegistry;
    private final AgentDebugStorageHandler agentDebugStorageHandler;
    private SupplementalInformationController supplementalInformationController;

    @Inject
    public AgentWorkerOperationFactory(CacheClient cacheClient,
            MetricRegistry metricRegistry,
            AgentDebugStorageHandler agentDebugStorageHandler, AgentWorkerOperationState agentWorkerOperationState,
            DebugAgentWorkerCommandState debugAgentWorkerCommandState,
            CircuitBreakerAgentWorkerCommandState circuitBreakerAgentWorkerCommandState,
            InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState,
            LoginAgentWorkerCommandState loginAgentWorkerCommandState,
            ReportProviderMetricsAgentWorkerCommandState reportProviderMetricsAgentWorkerCommandState,
            SupplementalInformationController supplementalInformationController,
            CryptoConfigurationDao cryptoConfigurationDao,
            ControllerWrapperProvider controllerWrapperProvider,
            AggregatorInfoProvider aggregatorInfoProvider,
            CuratorFramework coordinationClient,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        this.cacheClient = cacheClient;

        metricCacheLoader = new MetricCacheLoader(metricRegistry);
        this.cryptoConfigurationDao = cryptoConfigurationDao;
        this.controllerWrapperProvider = controllerWrapperProvider;
        this.aggregatorInfoProvider = aggregatorInfoProvider;

        // Initialize agent worker command states.
        this.agentWorkerOperationState = agentWorkerOperationState;
        this.debugAgentWorkerCommandState = debugAgentWorkerCommandState;
        circuitBreakAgentWorkerCommandState = circuitBreakerAgentWorkerCommandState;
        this.instantiateAgentWorkerCommandState = instantiateAgentWorkerCommandState;
        this.loginAgentWorkerCommandState = loginAgentWorkerCommandState;
        this.reportMetricsAgentWorkerCommandState = reportProviderMetricsAgentWorkerCommandState;

        this.metricRegistry = metricRegistry;
        this.agentDebugStorageHandler = agentDebugStorageHandler;
        this.supplementalInformationController = supplementalInformationController;
        this.coordinationClient = coordinationClient;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
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

    private List<AgentWorkerCommand> createRefreshableItemsChain(CredentialsRequest request, AgentWorkerCommandContext context,
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

    public AgentWorkerOperation createRefreshOperation(RefreshInformationRequest request,
            ClientInfo clientInfo) {
        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        log.debug("Creating refresh operation chain for credential");

        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());
        CryptoWrapper cryptoWrapper = cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = (request.isManual() ? "refresh-manual" : "refresh-auto");

        commands.add(new SetCredentialsStatusToAuthenticatingAgentWorkerCommand(controllerWrapper, request.getCredentials(), request.getProvider()));
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, metricsName, reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new DecryptCredentialsWorkerCommand(context, new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper)));
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)));
        commands.addAll(createRefreshAccountsCommandChain(request, context, request.getItemsToRefresh()));
        commands.add(new SelectAccountsToAggregateCommand(context, request));
        commands.addAll(createRefreshableItemsChain(request, context, request.getItemsToRefresh()));

        log.debug("Created refresh operation chain for credential");
        return new AgentWorkerOperation(agentWorkerOperationState, metricsName, request, commands, context);
    }

    public AgentWorkerOperation createExecuteTransferOperation(TransferRequest request,
            ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());

        String operationName = "execute-transfer";

        List<AgentWorkerCommand> commands = createTransferBaseCommands(clientInfo, request, context, operationName, controllerWrapper);
        commands.addAll(createRefreshAccountsCommandChain(request, context, RefreshableItem.REFRESHABLE_ITEMS_ALL));
        commands.add(new SelectAccountsToAggregateCommand(context, request));
        commands.addAll(createRefreshableItemsChain(request, context, RefreshableItem.REFRESHABLE_ITEMS_ALL));

        return new AgentWorkerOperation(agentWorkerOperationState, operationName, request, commands,
                context);
    }

    public AgentWorkerOperation createExecuteWhitelistedTransferOperation(WhitelistedTransferRequest request,
            ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());
        context.setWhitelistRefresh(true);

        String operationName = "execute-whitelisted-transfer";

        List<AgentWorkerCommand> commands = createTransferBaseCommands(clientInfo, request, context, operationName, controllerWrapper);
        commands.addAll(createWhitelistRefreshableItemsChain(request, context, RefreshableItem.REFRESHABLE_ITEMS_ALL, controllerWrapper));

        return new AgentWorkerOperation(agentWorkerOperationState, operationName, request, commands, context);
    }

    private List<AgentWorkerCommand> createTransferBaseCommands(ClientInfo clientInfo, TransferRequest request,
            AgentWorkerCommandContext context, String operationName, ControllerWrapper controllerWrapper) {

        CryptoWrapper cryptoWrapper = cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto = new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);

        return Lists.newArrayList(
                new ValidateProviderAgentWorkerStatus(context, controllerWrapper),
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState),
                new ReportProviderMetricsAgentWorkerCommand(context, operationName, reportMetricsAgentWorkerCommandState),
                new ReportProviderTransferMetricsAgentWorkerCommand(context,  operationName),
                new LockAgentWorkerCommand(context),
                new DecryptCredentialsWorkerCommand(context, credentialsCrypto),
                new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler),
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState),
                new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)),
                new TransferAgentWorkerCommand(context, request, createMetricState(request)));
    }

    public AgentWorkerOperation createCreateCredentialsOperation(CredentialsRequest request,
            ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());
        CryptoWrapper cryptoWrapper = cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto = new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));
        commands.add(new LockAgentWorkerCommand(context)); // acquire lock to avoid encryption/decryption race conditions
        commands.add(new EncryptCredentialsWorkerCommand( context, false, credentialsCrypto));

        return new AgentWorkerOperation(agentWorkerOperationState, "create-credentials", request, commands,
                context);
    }

    public AgentWorkerOperation createUpdateOperation(CredentialsRequest request,
            ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());
        CryptoWrapper cryptoWrapper = cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto = new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));
        commands.add(new LockAgentWorkerCommand(context));// acquire lock to avoid encryption/decryption race conditions
        commands.add(new EncryptCredentialsWorkerCommand(context, false, credentialsCrypto));

        return new AgentWorkerOperation(agentWorkerOperationState, "update-credentials", request, commands,
                context);
    }

    public AgentWorkerOperation createKeepAliveOperation(KeepAliveRequest request,
            ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                        supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());
        CryptoWrapper cryptoWrapper = cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto = new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, "keep-alive", reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new DecryptCredentialsWorkerCommand(context, credentialsCrypto));
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new KeepAliveAgentWorkerCommand(context));

        return new AgentWorkerOperation(agentWorkerOperationState, "keep-alive", request, commands, context);
    }

    public AgentWorkerOperation createReEncryptCredentialsOperation(ReEncryptCredentialsRequest request,
            ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                        supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());

        CryptoWrapper cryptoWrapper = cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        ImmutableList<AgentWorkerCommand> commands = ImmutableList.of(
                new LockAgentWorkerCommand(context),
                new DecryptCredentialsWorkerCommand(context, new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper)),
                new EncryptCredentialsWorkerCommand(context, new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper))
        );

        return new AgentWorkerOperation(agentWorkerOperationState, "reencrypt-credentials", request,
                commands, context);
    }

    // for each account type,
    private List<AgentWorkerCommand> createRefreshAccountsCommandChain(CredentialsRequest request,
            AgentWorkerCommandContext context, Set<RefreshableItem> itemsToRefresh) {

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
    public AgentWorkerOperation createWhitelistRefreshOperation(RefreshWhitelistInformationRequest request,
            ClientInfo clientInfo) {
        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        log.debug("Creating whitelist refresh operation chain for credential");

        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());

        context.setWhitelistRefresh(true);
        CryptoWrapper cryptoWrapper = cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto = new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = (request.isManual() ? "refresh-manual" : "refresh-auto");

        commands.add(new SetCredentialsStatusToAuthenticatingAgentWorkerCommand(controllerWrapper, request.getCredentials(), request.getProvider()));
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, metricsName, reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new DecryptCredentialsWorkerCommand(context, credentialsCrypto));
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, createMetricState(request)));
        commands.addAll(createWhitelistRefreshableItemsChain(request, context, request.getItemsToRefresh(), controllerWrapper));

        log.debug("Created whitelist refresh operation chain for credential");
        return new AgentWorkerOperation(agentWorkerOperationState, metricsName, request, commands, context);
    }

    /**
     *
     * Use this operation when changing whitelisted accounts and then doing a refresh.
     *
     */
    public AgentWorkerOperation createConfigureWhitelistOperation(ConfigureWhitelistInformationRequest request,
            ClientInfo clientInfo) {
        String operationMetricName = "configure-whitelist";

        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        ControllerWrapper controllerWrapper = controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        CryptoWrapper cryptoWrapper = cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        AgentWorkerCommandContext context = new AgentWorkerCommandContext(request, metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                supplementalInformationController,
                controllerWrapper, clientInfo.getClusterId());

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(new ReportProviderMetricsAgentWorkerCommand(context, operationMetricName, reportMetricsAgentWorkerCommandState));
        commands.add(new LockAgentWorkerCommand(context));
        commands.add(new DecryptCredentialsWorkerCommand(context, new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper)));
        commands.add(new DebugAgentWorkerCommand(context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new LoginAgentWorkerCommand(context, loginAgentWorkerCommandState, metricState(request)));
        commands.addAll(createPartialWhitelistRefreshableItems(request, context, request.getItemsToRefresh(), controllerWrapper));

        return new AgentWorkerOperation(agentWorkerOperationState, operationMetricName, request, commands, context);
    }

    private ImmutableList<AgentWorkerCommand> createWhitelistRefreshableItemsChain(CredentialsRequest request,
            AgentWorkerCommandContext context, Set<RefreshableItem> itemsToRefresh,
            ControllerWrapper controllerWrapper) {

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
                        (ConfigureWhitelistInformationRequest) request, controllerWrapper));
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
            Class<? extends Agent> agentClass = AgentClassFactory.getAgentClass(provider);
            return NextGenerationAgent.class.isAssignableFrom(agentClass);
        } catch (Exception e) {
            return false;
        }
    }

}
