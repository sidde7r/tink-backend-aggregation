package se.tink.backend.aggregation.workers.worker;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.api.WhitelistedTransferRequest;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.CredentialsEventProducer;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.ReEncryptCredentialsRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.providers.AggregatorInfoProvider;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ClearSensitiveInformationCommand;
import se.tink.backend.aggregation.workers.commands.CreateAgentConfigurationControllerWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CreateLogMaskerWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DebugAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DecryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.EncryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ExpireSessionAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.KeepAliveAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LockAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.MigrateCredentialWorkerCommand;
import se.tink.backend.aggregation.workers.commands.MigrateCredentialsAndAccountsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshCommandChainEventTriggerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderTransferMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RequestUserOptInAccountsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SelectAccountsToAggregateCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountsToDataAvailabilityTrackerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountsToUpdateServiceAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendDataForProcessingAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.TransferAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.UpdateCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ValidateProviderAgentWorkerStatus;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactory;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricCacheLoader;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation.AgentWorkerOperationState;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.backend.aggregation.workers.worker.conditions.annotation.ShouldAddExtraCommands;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.MigrateCredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

public class AgentWorkerOperationFactory {
    private static final Logger log = LoggerFactory.getLogger(AgentWorkerOperationFactory.class);

    private final CacheClient cacheClient;
    private final MetricCacheLoader metricCacheLoader;
    private final CryptoConfigurationDao cryptoConfigurationDao;
    private final ControllerWrapperProvider controllerWrapperProvider;
    private final AggregatorInfoProvider aggregatorInfoProvider;
    private final CuratorFramework coordinationClient;
    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final AgentDebugStorageHandler agentDebugStorageHandler;
    private final CredentialsEventProducer credentialsEventProducer;
    private final DataTrackerEventProducer dataTrackerEventProducer;
    private final LoginAgentEventProducer loginAgentEventProducer;
    private final Predicate<Provider> shouldAddExtraCommands;

    // States
    private AgentWorkerOperationState agentWorkerOperationState;
    private CircuitBreakerAgentWorkerCommandState circuitBreakAgentWorkerCommandState;
    private DebugAgentWorkerCommandState debugAgentWorkerCommandState;
    private InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState;
    private LoginAgentWorkerCommandState loginAgentWorkerCommandState;
    private ReportProviderMetricsAgentWorkerCommandState reportMetricsAgentWorkerCommandState;
    private MetricRegistry metricRegistry;
    private SupplementalInformationController supplementalInformationController;
    private ProviderSessionCacheController providerSessionCacheController;
    private AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient;
    private TppSecretsServiceClient tppSecretsServiceClient;
    private InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory;

    @Inject
    public AgentWorkerOperationFactory(
            CacheClient cacheClient,
            MetricRegistry metricRegistry,
            AgentDebugStorageHandler agentDebugStorageHandler,
            AgentWorkerOperationState agentWorkerOperationState,
            DebugAgentWorkerCommandState debugAgentWorkerCommandState,
            CircuitBreakerAgentWorkerCommandState circuitBreakerAgentWorkerCommandState,
            InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState,
            LoginAgentWorkerCommandState loginAgentWorkerCommandState,
            ReportProviderMetricsAgentWorkerCommandState
                    reportProviderMetricsAgentWorkerCommandState,
            SupplementalInformationController supplementalInformationController,
            ProviderSessionCacheController providerSessionCacheController,
            CryptoConfigurationDao cryptoConfigurationDao,
            ControllerWrapperProvider controllerWrapperProvider,
            AggregatorInfoProvider aggregatorInfoProvider,
            CuratorFramework coordinationClient,
            AgentsServiceConfiguration agentsServiceConfiguration,
            CredentialsEventProducer credentialsEventProducer,
            DataTrackerEventProducer dataTrackerEventProducer,
            LoginAgentEventProducer loginAgentEventProducer,
            AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient,
            ManagedTppSecretsServiceClient tppSecretsServiceClient,
            InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory,
            @ShouldAddExtraCommands Predicate<Provider> shouldAddExtraCommands) {
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
        this.providerSessionCacheController = providerSessionCacheController;
        this.coordinationClient = coordinationClient;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        this.credentialsEventProducer = credentialsEventProducer;
        this.dataTrackerEventProducer = dataTrackerEventProducer;
        this.loginAgentEventProducer = loginAgentEventProducer;
        this.agentDataAvailabilityTrackerClient = agentDataAvailabilityTrackerClient;
        this.tppSecretsServiceClient = tppSecretsServiceClient;
        this.interProcessSemaphoreMutexFactory = interProcessSemaphoreMutexFactory;
        this.shouldAddExtraCommands = shouldAddExtraCommands;
    }

    private AgentWorkerCommandMetricState createCommandMetricState(CredentialsRequest request) {
        return new AgentWorkerCommandMetricState(
                request.getProvider(),
                request.getCredentials(),
                metricCacheLoader,
                request.getType());
    }

    // Remove `ACCOUNTS` and `TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS` and replace them with
    // appropriate new
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

    private List<AgentWorkerCommand> createOrderedRefreshableItemsCommands(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh) {

        itemsToRefresh = convertLegacyItems(itemsToRefresh);

        // Sort the refreshable items
        List<RefreshableItem> items = RefreshableItem.sort(itemsToRefresh);

        log.info(
                "Items to refresh (sorted): {}",
                items.stream().map(Enum::name).collect(Collectors.joining(", ")));

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        List<RefreshableItem> accountItems =
                items.stream().filter(RefreshableItem::isAccount).collect(Collectors.toList());

        List<RefreshableItem> nonAccountItems =
                items.stream().filter(i -> !accountItems.contains(i)).collect(Collectors.toList());

        if (accountItems.size() > 0) {
            commands.add(
                    new SendAccountsToUpdateServiceAgentWorkerCommand(
                            context, createCommandMetricState(request)));
            commands.add(
                    new SendAccountsToDataAvailabilityTrackerAgentWorkerCommand(
                            context,
                            createCommandMetricState(request),
                            agentDataAvailabilityTrackerClient,
                            dataTrackerEventProducer));
        }

        for (RefreshableItem item : nonAccountItems) {
            commands.add(
                    new RefreshItemAgentWorkerCommand(
                            context,
                            item,
                            createCommandMetricState(request),
                            agentDataAvailabilityTrackerClient,
                            dataTrackerEventProducer));
        }

        // FIXME: remove when Handelsbanken and Avanza have been moved to the nextgen agents. (TOP
        // PRIO)
        // Due to the agents depending on updateTransactions to populate the the Accounts list
        // We need to reselect and send accounts to system
        if (shouldAddExtraCommands.test(request.getProvider())) {
            commands.add(new SelectAccountsToAggregateCommand(context, request));
            commands.add(
                    new SendAccountsToUpdateServiceAgentWorkerCommand(
                            context, createCommandMetricState(request)));
            commands.add(
                    new SendAccountsToDataAvailabilityTrackerAgentWorkerCommand(
                            context,
                            createCommandMetricState(request),
                            agentDataAvailabilityTrackerClient,
                            dataTrackerEventProducer));
        }

        return commands;
    }

    public AgentWorkerOperation createOperationRefresh(
            RefreshInformationRequest request, ClientInfo clientInfo) {
        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null
            // list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        log.debug("Creating refresh operation chain for credential");

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = generateOrGetCorrelationId(request.getRefreshId());

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);

        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        // Please be aware that the order of adding commands is meaningful
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = (request.isManual() ? "refresh-manual" : "refresh-auto");

        commands.add(
                new RefreshCommandChainEventTriggerCommand(
                        credentialsEventProducer,
                        context.getCorrelationId(),
                        request.getCredentials(),
                        clientInfo.getAppId(),
                        request.getItemsToRefresh(),
                        request.isManual(),
                        clientInfo.getClusterId()));
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.isManual(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(
                        context, metricsName, interProcessSemaphoreMutexFactory));
        commands.add(
                new DecryptCredentialsWorkerCommand(
                        context,
                        new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper)));
        commands.add(
                new MigrateCredentialsAndAccountsWorkerCommand(
                        context.getRequest(), controllerWrapper, clientInfo));

        // Update the status to `UPDATED` if the credential isn't waiting on transactions from the
        // connector and if
        // transactions aren't processed in system. The transaction processing in system will set
        // the status
        // to `UPDATED` when transactions have been processed and new statistics are generated.
        commands.add(
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        request.getCredentials(),
                        request.getProvider(),
                        context,
                        c ->
                                !c.isWaitingOnConnectorTransactions()
                                        && !c.isSystemProcessingTransactions()));
        commands.add(
                new ReportProviderMetricsAgentWorkerCommand(
                        context, metricsName, reportMetricsAgentWorkerCommandState));
        commands.add(
                new SendDataForProcessingAgentWorkerCommand(
                        context,
                        createCommandMetricState(request),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(request.getItemsToRefresh()))));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(
                new DebugAgentWorkerCommand(
                        context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(
                new LoginAgentWorkerCommand(
                        context,
                        loginAgentWorkerCommandState,
                        createCommandMetricState(request),
                        loginAgentEventProducer));
        commands.addAll(
                createRefreshAccountsCommands(request, context, request.getItemsToRefresh()));
        commands.add(new SelectAccountsToAggregateCommand(context, request));
        commands.addAll(
                createOrderedRefreshableItemsCommands(
                        request, context, request.getItemsToRefresh()));

        log.debug("Created refresh operation chain for credential");
        return new AgentWorkerOperation(
                agentWorkerOperationState, metricsName, request, commands, context);
    }

    public AgentWorkerOperation createOperationAuthenticate(
            ManualAuthenticateRequest request, ClientInfo clientInfo) {

        log.debug("Creating Authenticate operation chain for credential");

        request.setManualAuthentication();

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        // Please be aware that the order of adding commands is meaningful
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = (request.isManual() ? "authenticate-manual" : "authenticate-auto");

        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(
                        context, metricsName, interProcessSemaphoreMutexFactory));
        commands.add(
                new DecryptCredentialsWorkerCommand(
                        context,
                        new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper)));
        commands.add(
                new MigrateCredentialsAndAccountsWorkerCommand(
                        context.getRequest(), controllerWrapper, clientInfo));

        commands.add(
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        request.getCredentials(),
                        request.getProvider(),
                        context,
                        c ->
                                !c.isWaitingOnConnectorTransactions()
                                        && !c.isSystemProcessingTransactions()));
        commands.add(
                new ReportProviderMetricsAgentWorkerCommand(
                        context, metricsName, reportMetricsAgentWorkerCommandState));

        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(
                new DebugAgentWorkerCommand(
                        context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(
                new LoginAgentWorkerCommand(
                        context,
                        loginAgentWorkerCommandState,
                        createCommandMetricState(request),
                        loginAgentEventProducer));

        log.debug("Created Authenticate operation for credential");
        return new AgentWorkerOperation(
                agentWorkerOperationState, metricsName, request, commands, context);
    }

    public AgentWorkerOperation createOperationExecuteTransfer(
            TransferRequest request, ClientInfo clientInfo) {

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);

        String operationName;
        List<AgentWorkerCommand> commands;

        // TODO: PAY2-409 - Check if UK provider works with LoginCommand and fix
        if (isUKOBProvider(request.getProvider())) {
            operationName = "execute-transfer-without-refresh";
            commands =
                    createTransferWithoutRefreshBaseCommands(
                            clientInfo, request, context, operationName, controllerWrapper);

        } else {

            boolean shouldRefresh =
                    !request.getUser().getFlags().contains(FeatureFlags.ANONYMOUS)
                            || !request.isSkipRefresh();
            operationName =
                    shouldRefresh
                            ? "execute-transfer-with-refresh"
                            : "execute-transfer-without-refresh";
            commands =
                    createTransferBaseCommands(
                            clientInfo, request, context, operationName, controllerWrapper);
            if (shouldRefresh) {
                commands.addAll(
                        createRefreshAccountsCommands(
                                request, context, RefreshableItem.REFRESHABLE_ITEMS_ALL));
                commands.add(new SelectAccountsToAggregateCommand(context, request));
                commands.addAll(
                        createOrderedRefreshableItemsCommands(
                                request, context, RefreshableItem.REFRESHABLE_ITEMS_ALL));
            }
        }

        return new AgentWorkerOperation(
                agentWorkerOperationState, operationName, request, commands, context);
    }

    private boolean isUKOBProvider(Provider provider) {
        return provider.getMarket().equals(MarketCode.GB.toString()) && provider.isOpenBanking();
    }

    public AgentWorkerOperation createOperationExecuteWhitelistedTransfer(
            WhitelistedTransferRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);
        context.setWhitelistRefresh(true);

        String operationName = "execute-whitelisted-transfer";

        List<AgentWorkerCommand> commands =
                createTransferBaseCommands(
                        clientInfo, request, context, operationName, controllerWrapper);
        commands.addAll(
                createWhitelistRefreshableItemsCommands(
                        request,
                        context,
                        RefreshableItem.REFRESHABLE_ITEMS_ALL,
                        controllerWrapper));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operationName, request, commands, context);
    }

    private List<AgentWorkerCommand> createTransferBaseCommands(
            ClientInfo clientInfo,
            TransferRequest request,
            AgentWorkerCommandContext context,
            String operationName,
            ControllerWrapper controllerWrapper) {

        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);

        return Lists.newArrayList(
                new ValidateProviderAgentWorkerStatus(context, controllerWrapper),
                new ExpireSessionAgentWorkerCommand(
                        request.isManual(),
                        context,
                        request.getCredentials(),
                        request.getProvider()),
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState),
                new LockAgentWorkerCommand(
                        context, operationName, interProcessSemaphoreMutexFactory),
                new DecryptCredentialsWorkerCommand(context, credentialsCrypto),
                new MigrateCredentialsAndAccountsWorkerCommand(
                        context.getRequest(), controllerWrapper, clientInfo),
                // Update the status to `UPDATED` if the credential isn't waiting on transactions
                // from the
                // connector and if
                // transactions aren't processed in system. The transaction processing in system
                // will set
                // the status
                // to `UPDATED` when transactions have been processed and new statistics are
                // generated.
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        request.getCredentials(),
                        request.getProvider(),
                        context,
                        c ->
                                !c.isWaitingOnConnectorTransactions()
                                        && !c.isSystemProcessingTransactions()),
                new ReportProviderMetricsAgentWorkerCommand(
                        context, operationName, reportMetricsAgentWorkerCommandState),
                new ReportProviderTransferMetricsAgentWorkerCommand(context, operationName),
                new SendDataForProcessingAgentWorkerCommand(
                        context,
                        createCommandMetricState(request),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(
                                        RefreshableItem.REFRESHABLE_ITEMS_ALL))),
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient),
                new CreateLogMaskerWorkerCommand(context),
                new DebugAgentWorkerCommand(
                        context, debugAgentWorkerCommandState, agentDebugStorageHandler),
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState),
                new LoginAgentWorkerCommand(
                        context,
                        loginAgentWorkerCommandState,
                        createCommandMetricState(request),
                        loginAgentEventProducer),
                new TransferAgentWorkerCommand(
                        context, request, createCommandMetricState(request)));
    }

    private List<AgentWorkerCommand> createTransferWithoutRefreshBaseCommands(
            ClientInfo clientInfo,
            TransferRequest request,
            AgentWorkerCommandContext context,
            String operationName,
            ControllerWrapper controllerWrapper) {

        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);

        return Lists.newArrayList(
                new ValidateProviderAgentWorkerStatus(context, controllerWrapper),
                new ExpireSessionAgentWorkerCommand(
                        request.isManual(),
                        context,
                        request.getCredentials(),
                        request.getProvider()),
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState),
                new LockAgentWorkerCommand(
                        context, operationName, interProcessSemaphoreMutexFactory),
                new DecryptCredentialsWorkerCommand(context, credentialsCrypto),
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        request.getCredentials(),
                        request.getProvider(),
                        context,
                        c -> true), // is it enough to return true in this predicate?
                new ReportProviderMetricsAgentWorkerCommand(
                        context, operationName, reportMetricsAgentWorkerCommandState),
                new ReportProviderTransferMetricsAgentWorkerCommand(context, operationName),
                new SendDataForProcessingAgentWorkerCommand( // todo @majid investigate if this is
                        // needed?
                        context,
                        createCommandMetricState(request),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(
                                        RefreshableItem.REFRESHABLE_ITEMS_ALL))),
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient),
                new CreateLogMaskerWorkerCommand(context),
                new DebugAgentWorkerCommand(
                        context, debugAgentWorkerCommandState, agentDebugStorageHandler),
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState),
                new TransferAgentWorkerCommand(
                        context, request, createCommandMetricState(request)));
    }

    public AgentWorkerOperation createOperationCreateCredentials(
            CredentialsRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));
        String operation = "create-credentials";
        // acquire lock to avoid encryption/decryption race conditions
        commands.add(
                new LockAgentWorkerCommand(context, operation, interProcessSemaphoreMutexFactory));
        commands.add(new EncryptCredentialsWorkerCommand(context, false, credentialsCrypto));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operation, request, commands, context);
    }

    public AgentWorkerOperation createOperationUpdate(
            CredentialsRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));
        String operation = "update-credentials";
        // acquire lock to avoid encryption/decryption race conditions
        commands.add(
                new LockAgentWorkerCommand(context, operation, interProcessSemaphoreMutexFactory));
        commands.add(new EncryptCredentialsWorkerCommand(context, false, credentialsCrypto));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operation, request, commands, context);
    }

    public AgentWorkerOperation createOperationKeepAlive(
            KeepAliveRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        String operation = "keep-alive";
        commands.add(
                new LockAgentWorkerCommand(context, operation, interProcessSemaphoreMutexFactory));
        commands.add(new DecryptCredentialsWorkerCommand(context, credentialsCrypto));
        commands.add(
                new MigrateCredentialsAndAccountsWorkerCommand(
                        context.getRequest(), controllerWrapper, clientInfo));
        commands.add(
                new ReportProviderMetricsAgentWorkerCommand(
                        context, operation, reportMetricsAgentWorkerCommandState));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(
                new DebugAgentWorkerCommand(
                        context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(new KeepAliveAgentWorkerCommand(context));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operation, request, commands, context);
    }

    public AgentWorkerOperation createOperationReEncryptCredentials(
            ReEncryptCredentialsRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);

        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        String operation = "reencrypt-credentials";
        ImmutableList<AgentWorkerCommand> commands =
                ImmutableList.of(
                        new LockAgentWorkerCommand(
                                context, operation, interProcessSemaphoreMutexFactory),
                        new DecryptCredentialsWorkerCommand(
                                context,
                                new CredentialsCrypto(
                                        cacheClient, controllerWrapper, cryptoWrapper)),
                        new EncryptCredentialsWorkerCommand(
                                context,
                                new CredentialsCrypto(
                                        cacheClient, controllerWrapper, cryptoWrapper)));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operation, request, commands, context);
    }

    // for each account type,
    private List<AgentWorkerCommand> createRefreshAccountsCommands(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh) {

        List<RefreshableItem> items = RefreshableItem.sort(convertLegacyItems(itemsToRefresh));

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        for (RefreshableItem item : items) {
            if (RefreshableItem.isAccount(item)) {
                commands.add(
                        new RefreshItemAgentWorkerCommand(
                                context,
                                item,
                                createCommandMetricState(request),
                                agentDataAvailabilityTrackerClient,
                                dataTrackerEventProducer));
            }
        }

        return commands;
    }

    /** Use this operation when refreshing only the accounts that are available in the request. */
    public AgentWorkerOperation createOperationWhitelistRefresh(
            RefreshWhitelistInformationRequest request, ClientInfo clientInfo) {
        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null
            // list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        log.debug("Creating whitelist refresh operation chain for credential");

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = generateOrGetCorrelationId(request.getRefreshId());

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);

        context.setWhitelistRefresh(true);
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = (request.isManual() ? "refresh-manual" : "refresh-auto");

        commands.add(
                new RefreshCommandChainEventTriggerCommand(
                        credentialsEventProducer,
                        context.getCorrelationId(),
                        request.getCredentials(),
                        clientInfo.getAppId(),
                        request.getItemsToRefresh(),
                        request.isManual(),
                        clientInfo.getClusterId()));
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.isManual(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(
                        context, metricsName, interProcessSemaphoreMutexFactory));
        commands.add(new DecryptCredentialsWorkerCommand(context, credentialsCrypto));
        commands.add(
                new MigrateCredentialsAndAccountsWorkerCommand(
                        context.getRequest(), controllerWrapper, clientInfo));
        // Update the status to `UPDATED` if the credential isn't waiting on transactions from the
        // connector and if
        // transactions aren't processed in system. The transaction processing in system will set
        // the status
        // to `UPDATED` when transactions have been processed and new statistics are generated.
        commands.add(
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        request.getCredentials(),
                        request.getProvider(),
                        context,
                        c ->
                                !c.isWaitingOnConnectorTransactions()
                                        && !c.isSystemProcessingTransactions()));
        commands.add(
                new ReportProviderMetricsAgentWorkerCommand(
                        context, metricsName, reportMetricsAgentWorkerCommandState));
        commands.add(
                new SendDataForProcessingAgentWorkerCommand(
                        context,
                        createCommandMetricState(request),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(request.getItemsToRefresh()))));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(
                new DebugAgentWorkerCommand(
                        context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(
                new LoginAgentWorkerCommand(
                        context,
                        loginAgentWorkerCommandState,
                        createCommandMetricState(request),
                        loginAgentEventProducer));
        commands.addAll(
                createWhitelistRefreshableItemsCommands(
                        request, context, request.getItemsToRefresh(), controllerWrapper));

        log.debug("Created whitelist refresh operation chain for credential");
        return new AgentWorkerOperation(
                agentWorkerOperationState, metricsName, request, commands, context);
    }

    /** Use this operation when changing whitelisted accounts and then doing a refresh. */
    public AgentWorkerOperation createOperationConfigureWhitelist(
            ConfigureWhitelistInformationRequest request, ClientInfo clientInfo) {
        String operationMetricName = "configure-whitelist";

        if (request.getItemsToRefresh() == null || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null
            // list.
            // Instead it should abort if it's empty (empty list == do nothing).
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        final String correlationId = generateOrGetCorrelationId(request.getRefreshId());

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(
                new RefreshCommandChainEventTriggerCommand(
                        credentialsEventProducer,
                        context.getCorrelationId(),
                        request.getCredentials(),
                        clientInfo.getAppId(),
                        request.getItemsToRefresh(),
                        request.isManual(),
                        clientInfo.getClusterId()));
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.isManual(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(
                        context, operationMetricName, interProcessSemaphoreMutexFactory));
        commands.add(
                new DecryptCredentialsWorkerCommand(
                        context,
                        new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper)));
        commands.add(
                new MigrateCredentialsAndAccountsWorkerCommand(
                        context.getRequest(), controllerWrapper, clientInfo));
        // Update the status to `UPDATED` if the credential isn't waiting on transactions from the
        // connector and if
        // transactions aren't processed in system. The transaction processing in system will set
        // the status
        // to `UPDATED` when transactions have been processed and new statistics are generated.
        commands.add(
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        request.getCredentials(),
                        request.getProvider(),
                        context,
                        c ->
                                !c.isWaitingOnConnectorTransactions()
                                        && !c.isSystemProcessingTransactions()));
        commands.add(
                new ReportProviderMetricsAgentWorkerCommand(
                        context, operationMetricName, reportMetricsAgentWorkerCommandState));
        commands.add(
                new SendDataForProcessingAgentWorkerCommand(
                        context,
                        createCommandMetricState(request),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(request.getItemsToRefresh()))));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(
                new DebugAgentWorkerCommand(
                        context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(
                new LoginAgentWorkerCommand(
                        context,
                        loginAgentWorkerCommandState,
                        createCommandMetricState(request),
                        loginAgentEventProducer));
        commands.addAll(
                createWhitelistRefreshableItemsCommands(
                        request, context, request.getItemsToRefresh(), controllerWrapper));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operationMetricName, request, commands, context);
    }

    private ImmutableList<AgentWorkerCommand> createWhitelistRefreshableItemsCommands(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh,
            ControllerWrapper controllerWrapper) {

        // Convert legacy items to corresponding new refreshable items
        itemsToRefresh = convertLegacyItems(itemsToRefresh);

        // Sort the refreshable items
        List<RefreshableItem> items = RefreshableItem.sort(itemsToRefresh);
        log.info(
                "Items to refresh (sorted): {}",
                items.stream().map(Enum::name).collect(Collectors.joining(", ")));

        ImmutableList.Builder<AgentWorkerCommand> commands = ImmutableList.builder();

        Set<RefreshableItem> accountItems =
                items.stream().filter(RefreshableItem::isAccount).collect(Collectors.toSet());

        // === START REFRESHING ===
        if (accountItems.size() > 0) {
            // Start refreshing all account items
            commands.addAll(createRefreshAccountsCommands(request, context, accountItems));

            // If this is an optIn request we request the caller do supply supplemental information
            // with the
            // accounts they want to whitelist.
            if (request instanceof ConfigureWhitelistInformationRequest) {
                commands.add(
                        new RequestUserOptInAccountsAgentWorkerCommand(
                                context,
                                (ConfigureWhitelistInformationRequest) request,
                                controllerWrapper));
            }

            // Update the accounts on system side
            commands.add(new SelectAccountsToAggregateCommand(context, request));
            commands.add(
                    new SendAccountsToUpdateServiceAgentWorkerCommand(
                            context, createCommandMetricState(request)));
            commands.add(
                    new SendAccountsToDataAvailabilityTrackerAgentWorkerCommand(
                            context,
                            createCommandMetricState(request),
                            agentDataAvailabilityTrackerClient,
                            dataTrackerEventProducer));
        }

        // Add all refreshable items that aren't accounts to refresh them.
        items.stream()
                .filter(i -> !accountItems.contains(i))
                .forEach(
                        item ->
                                commands.add(
                                        new RefreshItemAgentWorkerCommand(
                                                context,
                                                item,
                                                createCommandMetricState(request),
                                                agentDataAvailabilityTrackerClient,
                                                dataTrackerEventProducer)));
        // === END REFRESHING ===
        return commands.build();
    }

    public AgentWorkerOperation createOperationMigrate(
            MigrateCredentialsRequest request, ClientInfo clientInfo, int targetVersion) {

        log.debug("Creating migration operation chain for credential");

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId);
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        // Please be aware that the order of adding commands is meaningful
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        String metricsName = "batch-migrate";

        commands.add(
                new LockAgentWorkerCommand(
                        context, metricsName, interProcessSemaphoreMutexFactory));
        commands.add(
                new DecryptCredentialsWorkerCommand(
                        context,
                        new CredentialsCrypto(cacheClient, controllerWrapper, cryptoWrapper),
                        false));
        commands.add(
                new MigrateCredentialWorkerCommand(
                        context.getRequest(),
                        clientInfo,
                        targetVersion,
                        context,
                        controllerWrapper));

        log.debug("Created migration operation chain for credential");
        return new AgentWorkerOperation(
                agentWorkerOperationState, metricsName, request, commands, context);
    }

    private static String generateOrGetCorrelationId(String correlationId) {
        if (correlationId == null) {
            return UUIDUtils.generateUUID();
        }
        return correlationId;
    }
}
