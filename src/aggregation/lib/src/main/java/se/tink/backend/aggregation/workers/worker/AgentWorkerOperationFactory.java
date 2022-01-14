package se.tink.backend.aggregation.workers.worker;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import no.finn.unleash.UnleashContext;
import org.apache.curator.framework.CuratorFramework;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.api.WhitelistedTransferRequest;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.Psd2PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.regulatory_restrictions.RegulatoryRestrictions;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderTierConfiguration;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.eidasidentity.CertificateIdProvider;
import se.tink.backend.aggregation.events.AccountHolderRefreshedEventProducer;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.events.CredentialsEventProducer;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.events.EventSender;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.events.RefreshEventProducer;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.CreateBeneficiaryCredentialsRequest;
import se.tink.backend.aggregation.rpc.ReEncryptCredentialsRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.providers.AggregatorInfoProvider;
import se.tink.backend.aggregation.storage.database.providers.ControllerWrapperProvider;
import se.tink.backend.aggregation.storage.logs.AgentHttpLogsStorageHandler;
import se.tink.backend.aggregation.workers.agent_metrics.AgentWorkerMetricReporter;
import se.tink.backend.aggregation.workers.commands.AbnAmroSpecificCase;
import se.tink.backend.aggregation.workers.commands.AccountSegmentRestrictionWorkerCommand;
import se.tink.backend.aggregation.workers.commands.AccountWhitelistRestrictionWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ClearSensitiveInformationCommand;
import se.tink.backend.aggregation.workers.commands.ClearSensitivePayloadOnForceAuthenticateCommand;
import se.tink.backend.aggregation.workers.commands.CreateAgentConfigurationControllerWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CreateCertIdWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CreateLogMaskerWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DataFetchingRestrictionWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DebugAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DecryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.EmitEventsAfterRefreshAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.EncryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ExpireSessionAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.FetcherInstrumentationAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LockAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LogRefreshSummaryAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.MigrateCredentialsAndAccountsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.Psd2PaymentAccountRestrictionWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshCommandChainEventTriggerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshItemAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RefreshPostProcessingAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderTransferMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RequestUserOptInAccountsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.RequestedAccountsRestrictionWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountRestrictionEventsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountSourceInfoEventWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendAccountsToUpdateServiceAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendDataForProcessingAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SetCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SetImpossibleToAbortRequestStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SetInitialAndFinalOperationStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.TransactionRefreshScopeFilteringCommand;
import se.tink.backend.aggregation.workers.commands.TransferAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.UpdateCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ValidateProviderAgentWorkerStatus;
import se.tink.backend.aggregation.workers.commands.exceptions.ExceptionProcessor;
import se.tink.backend.aggregation.workers.commands.payment.PaymentExecutionService;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactory;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation.AgentWorkerOperationState;
import se.tink.backend.aggregation.workers.operation.RequestStatusManager;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.backend.aggregation.workers.worker.beneficiary.CreateBeneficiaryAgentWorkerCommandOperation;
import se.tink.backend.aggregation.workers.worker.conditions.annotation.ShouldAddExtraCommands;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceInternalClient;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.backend.secretsservice.client.SecretsServiceInternalClient;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.payments_validations.java.se.tink.libraries.payments.validations.ProviderBasedValidationsUtil;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants;
import se.tink.libraries.uuid.UUIDUtils;

public class AgentWorkerOperationFactory {
    private static final Logger log = LoggerFactory.getLogger(AgentWorkerOperationFactory.class);

    private final CacheClient cacheClient;
    private final CryptoConfigurationDao cryptoConfigurationDao;
    private final ControllerWrapperProvider controllerWrapperProvider;
    private final AggregatorInfoProvider aggregatorInfoProvider;
    private final CuratorFramework coordinationClient;
    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final AgentHttpLogsStorageHandler agentHttpLogsStorageHandler;
    private final CredentialsEventProducer credentialsEventProducer;
    private final DataTrackerEventProducer dataTrackerEventProducer;
    private final LoginAgentEventProducer loginAgentEventProducer;
    private final RefreshEventProducer refreshEventProducer;
    private final ProviderTierConfiguration providerTierConfiguration;
    private final Predicate<Provider> shouldAddExtraCommands;
    private final RequestStatusManager requestStatusManager;

    // States
    private final AgentWorkerOperationState agentWorkerOperationState;
    private final CircuitBreakerAgentWorkerCommandState circuitBreakAgentWorkerCommandState;
    private final InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState;
    private final LoginAgentWorkerCommandState loginAgentWorkerCommandState;
    private final ReportProviderMetricsAgentWorkerCommandState reportMetricsAgentWorkerCommandState;
    private final MetricRegistry metricRegistry;
    private final SupplementalInformationController supplementalInformationController;
    private final ProviderSessionCacheController providerSessionCacheController;
    private final TppSecretsServiceClient tppSecretsServiceClient;
    private final SecretsServiceInternalClient secretsServiceInternalClient;
    private final InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory;
    private final UnleashClient unleashClient;
    private final RegulatoryRestrictions regulatoryRestrictions;
    private final Psd2PaymentAccountClassifier psd2PaymentAccountClassifier;
    private final AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;
    private final CertificateIdProvider certificateIdProvider;
    private final AccountHolderRefreshedEventProducer accountHolderRefreshedEventProducer;
    private final EventSender eventSender;
    private final ExceptionProcessor exceptionProcessor;
    private final PaymentExecutionService paymentExecutionService;

    @Inject
    public AgentWorkerOperationFactory(
            CacheClient cacheClient,
            MetricRegistry metricRegistry,
            AgentHttpLogsStorageHandler agentHttpLogsStorageHandler,
            AgentWorkerOperationState agentWorkerOperationState,
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
            RefreshEventProducer refreshEventProducer,
            ManagedTppSecretsServiceClient tppSecretsServiceClient,
            ManagedTppSecretsServiceInternalClient secretsServiceInternalClient,
            InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory,
            ProviderTierConfiguration providerTierConfiguration,
            @ShouldAddExtraCommands Predicate<Provider> shouldAddExtraCommands,
            RegulatoryRestrictions regulatoryRestrictions,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer,
            UnleashClient unleashClient,
            CertificateIdProvider certificateIdProvider,
            RequestStatusManager requestStatusManager,
            AccountHolderRefreshedEventProducer accountHolderRefreshedEventProducer,
            ExceptionProcessor exceptionProcessor,
            EventSender eventSender,
            PaymentExecutionService paymentExecutionService) {
        this.cacheClient = cacheClient;
        this.cryptoConfigurationDao = cryptoConfigurationDao;
        this.controllerWrapperProvider = controllerWrapperProvider;
        this.aggregatorInfoProvider = aggregatorInfoProvider;

        // Initialize agent worker command states.
        this.agentWorkerOperationState = agentWorkerOperationState;
        this.circuitBreakAgentWorkerCommandState = circuitBreakerAgentWorkerCommandState;
        this.instantiateAgentWorkerCommandState = instantiateAgentWorkerCommandState;
        this.loginAgentWorkerCommandState = loginAgentWorkerCommandState;
        this.reportMetricsAgentWorkerCommandState = reportProviderMetricsAgentWorkerCommandState;

        this.metricRegistry = metricRegistry;
        this.agentHttpLogsStorageHandler = agentHttpLogsStorageHandler;
        this.supplementalInformationController = supplementalInformationController;
        this.providerSessionCacheController = providerSessionCacheController;
        this.coordinationClient = coordinationClient;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        this.credentialsEventProducer = credentialsEventProducer;
        this.dataTrackerEventProducer = dataTrackerEventProducer;
        this.loginAgentEventProducer = loginAgentEventProducer;
        this.refreshEventProducer = refreshEventProducer;
        this.tppSecretsServiceClient = tppSecretsServiceClient;
        this.secretsServiceInternalClient = secretsServiceInternalClient;
        this.interProcessSemaphoreMutexFactory = interProcessSemaphoreMutexFactory;
        this.providerTierConfiguration = providerTierConfiguration;
        this.shouldAddExtraCommands = shouldAddExtraCommands;
        this.regulatoryRestrictions = regulatoryRestrictions;
        this.psd2PaymentAccountClassifier =
                Psd2PaymentAccountClassifier.createWithMetrics(metricRegistry);
        this.accountInformationServiceEventsProducer = accountInformationServiceEventsProducer;
        this.unleashClient = unleashClient;
        this.certificateIdProvider = certificateIdProvider;
        this.requestStatusManager = requestStatusManager;
        this.accountHolderRefreshedEventProducer = accountHolderRefreshedEventProducer;
        this.eventSender = eventSender;
        this.exceptionProcessor = exceptionProcessor;
        this.paymentExecutionService = paymentExecutionService;
    }

    private AgentWorkerCommandMetricState createCommandMetricState(
            CredentialsRequest request, ClientInfo clientInfo) {
        return new AgentWorkerCommandMetricState(
                request, metricRegistry, request.getType(), clientInfo);
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

        if (items.contains(RefreshableItem.EINVOICES)) {
            items =
                    items.stream()
                            .filter(item -> !RefreshableItem.EINVOICES.equals(item))
                            .collect(Collectors.toSet());
        }

        return items;
    }

    private List<AgentWorkerCommand> createOrderedRefreshableItemsCommands(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh,
            ControllerWrapper controllerWrapper,
            ClientInfo clientInfo) {

        if (isBalanceCalculationEnabled(context)) {
            log.debug("[BALANCE CALCULATOR] Enabled");

            return createOrderedRefreshableItemsCommandsWithChanges(
                    request, context, itemsToRefresh, controllerWrapper, clientInfo);
        }

        log.debug("[BALANCE CALCULATOR] Disabled");
        return createOrderedRefreshableItemsCommandsWithoutChanges(
                request, context, itemsToRefresh, controllerWrapper, clientInfo);
    }

    private List<AgentWorkerCommand> createOrderedRefreshableItemsCommandsWithChanges(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh,
            ControllerWrapper controllerWrapper,
            ClientInfo clientInfo) {
        itemsToRefresh = convertLegacyItems(itemsToRefresh);

        // Sort the refreshable items
        List<RefreshableItem> items = RefreshableItem.sort(itemsToRefresh);

        log.info(
                "Items to refresh (sorted): {}",
                items.stream().map(Enum::name).collect(Collectors.joining(", ")));

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        List<RefreshableItem> accountItems =
                items.stream().filter(RefreshableItem::isAccount).collect(Collectors.toList());

        List<RefreshableItem> transactionItems =
                items.stream().filter(RefreshableItem::isTransaction).collect(Collectors.toList());

        List<RefreshableItem> nonAccountAndNonTransactionItems =
                items.stream()
                        .filter(i -> !accountItems.contains(i) && !transactionItems.contains(i))
                        .collect(Collectors.toList());

        for (RefreshableItem item : transactionItems) {
            commands.add(
                    new RefreshItemAgentWorkerCommand(
                            context,
                            item,
                            createCommandMetricState(request, clientInfo),
                            refreshEventProducer));
        }

        commands.add(
                new RefreshPostProcessingAgentWorkerCommand(
                        context, createCommandMetricState(request, clientInfo)));

        if (accountItems.size() > 0) {
            commands.add(
                    new SendAccountsToUpdateServiceAgentWorkerCommand(
                            context, createCommandMetricState(request, clientInfo)));

            for (RefreshableItem item : nonAccountAndNonTransactionItems) {
                commands.add(
                        new RefreshItemAgentWorkerCommand(
                                context,
                                item,
                                createCommandMetricState(request, clientInfo),
                                refreshEventProducer));
            }

            commands.add(
                    new SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            psd2PaymentAccountClassifier,
                            controllerWrapper,
                            false));

            /* Special command; see {@link AbnAmroSpecificCase} for more information. */
            if (Objects.equals("abnamro.AbnAmroAgent", request.getProvider().getClassName())
                    && Objects.equals("nl-abnamro", request.getProvider().getName())) {
                commands.add(new AbnAmroSpecificCase(context));
            }

            commands.add(new FetcherInstrumentationAgentWorkerCommand(context, itemsToRefresh));
        }

        commands.add(
                new TransactionRefreshScopeFilteringCommand(
                        context.getAccountDataCache(), request));

        if (accountItems.size() > 0) {
            commands.add(
                    new EmitEventsAfterRefreshAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            dataTrackerEventProducer,
                            accountHolderRefreshedEventProducer,
                            items,
                            eventSender));
        }

        // FIXME: remove when Handelsbanken and Avanza have been moved to the nextgen agents. (TOP
        // PRIO)
        // Due to the agents depending on updateTransactions to populate the the Accounts list
        // We need to reselect and send accounts to system
        if (shouldAddExtraCommands.test(request.getProvider())) {
            commands.add(
                    new SendAccountSourceInfoEventWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new Psd2PaymentAccountRestrictionWorkerCommand(
                            context,
                            request,
                            regulatoryRestrictions,
                            psd2PaymentAccountClassifier,
                            accountInformationServiceEventsProducer,
                            controllerWrapper));
            commands.add(new DataFetchingRestrictionWorkerCommand(context, controllerWrapper));
            commands.add(new AccountSegmentRestrictionWorkerCommand(context));
            commands.add(new AccountWhitelistRestrictionWorkerCommand(context, request));
            commands.add(new RequestedAccountsRestrictionWorkerCommand(context));
            // SendAccountRestrictionEventsWorkerCommand should be added after all restrictions on
            // accounts have been made
            commands.add(
                    new SendAccountRestrictionEventsWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new SendAccountsToUpdateServiceAgentWorkerCommand(
                            context, createCommandMetricState(request, clientInfo)));
            commands.add(
                    new SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            psd2PaymentAccountClassifier,
                            controllerWrapper,
                            false));
            commands.add(
                    new EmitEventsAfterRefreshAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            dataTrackerEventProducer,
                            accountHolderRefreshedEventProducer,
                            items,
                            eventSender));
        }

        return commands;
    }

    private List<AgentWorkerCommand> createOrderedRefreshableItemsCommandsWithoutChanges(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh,
            ControllerWrapper controllerWrapper,
            ClientInfo clientInfo) {
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
                            context, createCommandMetricState(request, clientInfo)));
            commands.add(
                    new SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            psd2PaymentAccountClassifier,
                            controllerWrapper,
                            false));

            /* Special command; see {@link AbnAmroSpecificCase} for more information. */
            if (Objects.equals("abnamro.AbnAmroAgent", request.getProvider().getClassName())
                    && Objects.equals("nl-abnamro", request.getProvider().getName())) {
                commands.add(new AbnAmroSpecificCase(context));
            }

            commands.add(new FetcherInstrumentationAgentWorkerCommand(context, itemsToRefresh));
        }

        for (RefreshableItem item : nonAccountItems) {
            commands.add(
                    new RefreshItemAgentWorkerCommand(
                            context,
                            item,
                            createCommandMetricState(request, clientInfo),
                            refreshEventProducer));
        }

        commands.add(
                new TransactionRefreshScopeFilteringCommand(
                        context.getAccountDataCache(), request));

        if (accountItems.size() > 0) {
            commands.add(
                    new EmitEventsAfterRefreshAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            dataTrackerEventProducer,
                            accountHolderRefreshedEventProducer,
                            items,
                            eventSender));
        }

        // FIXME: remove when Handelsbanken and Avanza have been moved to the nextgen agents. (TOP
        // PRIO)
        // Due to the agents depending on updateTransactions to populate the the Accounts list
        // We need to reselect and send accounts to system
        if (shouldAddExtraCommands.test(request.getProvider())) {
            commands.add(
                    new SendAccountSourceInfoEventWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new Psd2PaymentAccountRestrictionWorkerCommand(
                            context,
                            request,
                            regulatoryRestrictions,
                            psd2PaymentAccountClassifier,
                            accountInformationServiceEventsProducer,
                            controllerWrapper));
            commands.add(new DataFetchingRestrictionWorkerCommand(context, controllerWrapper));
            commands.add(new AccountSegmentRestrictionWorkerCommand(context));
            commands.add(new AccountWhitelistRestrictionWorkerCommand(context, request));
            commands.add(new RequestedAccountsRestrictionWorkerCommand(context));
            // SendAccountRestrictionEventsWorkerCommand should be added after all restrictions on
            // accounts have been made
            commands.add(
                    new SendAccountRestrictionEventsWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new SendAccountsToUpdateServiceAgentWorkerCommand(
                            context, createCommandMetricState(request, clientInfo)));
            commands.add(
                    new SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            psd2PaymentAccountClassifier,
                            controllerWrapper,
                            false));
            commands.add(
                    new EmitEventsAfterRefreshAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            dataTrackerEventProducer,
                            accountHolderRefreshedEventProducer,
                            items,
                            eventSender));
        }

        return commands;
    }

    public AgentWorkerOperation createOperationRefresh(
            RefreshInformationRequest request, ClientInfo clientInfo) {
        if (Objects.isNull(request.getItemsToRefresh()) || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null
            // list.
            // Instead it should abort if it's empty (empty list == do nothing).
            log.info("createOperationRefresh called with empty or null itemsToRefresh.");
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }
        log.debug("Creating refresh operation chain for credential");

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = generateOrGetCorrelationId(request.getRefreshId());

        String metricsName = (request.isUserPresent() ? "refresh-manual" : "refresh-auto");

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
                        metricsName,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        // Please be aware that the order of adding commands is meaningful
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(
                new RefreshCommandChainEventTriggerCommand(
                        credentialsEventProducer,
                        context.getCorrelationId(),
                        request.getCredentials(),
                        clientInfo.getAppId(),
                        request.getItemsToRefresh(),
                        request.isUserPresent(),
                        clientInfo.getClusterId()));
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.getUserAvailability().isUserAvailableForInteraction(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(context, metricsName, interProcessSemaphoreMutexFactory)
                        .withLoginEvent(loginAgentEventProducer));

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetInitialAndFinalOperationStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        commands.add(
                new DecryptCredentialsWorkerCommand(
                        context,
                        new CredentialsCrypto(
                                cacheClient, controllerWrapper, cryptoWrapper, metricRegistry)));
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
                        context,
                        metricsName,
                        reportMetricsAgentWorkerCommandState,
                        new AgentWorkerMetricReporter(
                                metricRegistry, this.providerTierConfiguration)));
        commands.add(
                new SendDataForProcessingAgentWorkerCommand(
                        context,
                        createCommandMetricState(request, clientInfo),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(request.getItemsToRefresh()))));
        commands.add(new CreateCertIdWorkerCommand(context, certificateIdProvider));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient, secretsServiceInternalClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(new DebugAgentWorkerCommand(context, agentHttpLogsStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
                commands, context, clientInfo);

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetImpossibleToAbortRequestStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        commands.add(
                new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));
        commands.addAll(
                createRefreshAccountsCommands(
                        request, context, request.getItemsToRefresh(), clientInfo));
        commands.add(
                new SendAccountSourceInfoEventWorkerCommand(
                        context, accountInformationServiceEventsProducer));
        commands.add(
                new Psd2PaymentAccountRestrictionWorkerCommand(
                        context,
                        request,
                        regulatoryRestrictions,
                        psd2PaymentAccountClassifier,
                        accountInformationServiceEventsProducer,
                        controllerWrapper));
        commands.add(new DataFetchingRestrictionWorkerCommand(context, controllerWrapper));
        commands.add(new AccountSegmentRestrictionWorkerCommand(context));
        commands.add(new AccountWhitelistRestrictionWorkerCommand(context, request));
        commands.add(new RequestedAccountsRestrictionWorkerCommand(context));
        // SendAccountRestrictionEventsWorkerCommand should be added after all restrictions on
        // accounts have been made
        commands.add(
                new SendAccountRestrictionEventsWorkerCommand(
                        context, accountInformationServiceEventsProducer));
        commands.addAll(
                createOrderedRefreshableItemsCommands(
                        request,
                        context,
                        request.getItemsToRefresh(),
                        controllerWrapper,
                        clientInfo));

        commands.add(new LogRefreshSummaryAgentWorkerCommand(context));

        log.debug("Created refresh operation chain for credential");
        return new AgentWorkerOperation(
                agentWorkerOperationState, metricsName, request, commands, context);
    }

    public AgentWorkerOperation createOperationAuthenticate(
            ManualAuthenticateRequest request, ClientInfo clientInfo) {

        log.debug("Creating Authenticate operation chain for credential");

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = generateOrGetCorrelationId(request.getOperationId());

        String metricsName =
                (request.isUserPresent() ? "authenticate-manual" : "authenticate-auto");

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
                        metricsName,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        // Please be aware that the order of adding commands is meaningful
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.getUserAvailability().isUserAvailableForInteraction(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(context, metricsName, interProcessSemaphoreMutexFactory)
                        .withLoginEvent(loginAgentEventProducer));

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetInitialAndFinalOperationStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        commands.add(
                new DecryptCredentialsWorkerCommand(
                        context,
                        new CredentialsCrypto(
                                cacheClient, controllerWrapper, cryptoWrapper, metricRegistry)));
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
                        context,
                        metricsName,
                        reportMetricsAgentWorkerCommandState,
                        new AgentWorkerMetricReporter(
                                metricRegistry, this.providerTierConfiguration)));

        commands.add(new CreateCertIdWorkerCommand(context, certificateIdProvider));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient, secretsServiceInternalClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(new DebugAgentWorkerCommand(context, agentHttpLogsStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));

        addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
                commands, context, clientInfo);

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetImpossibleToAbortRequestStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        commands.add(
                new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));

        log.debug("Created Authenticate operation for credential");
        return new AgentWorkerOperation(
                agentWorkerOperationState, metricsName, request, commands, context);
    }

    public AgentWorkerOperation createOperationExecuteTransfer(
            TransferRequest request, ClientInfo clientInfo) {

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());
        final String correlationId = UUIDUtils.generateUUID();

        String operationName;
        AgentWorkerCommandContext context;
        List<AgentWorkerCommand> commands;

        // TODO: PAY2-409 - Check if UK provider works with LoginCommand and fix
        if (isUKOBProvider(request.getProvider()) || isFrenchTestProvider(request.getProvider())) {

            operationName = "legacy-execute-transfer";
            context =
                    createContentForExecuteTransfer(
                            request, clientInfo, controllerWrapper, correlationId, operationName);
            commands =
                    createTransferWithoutRefreshBaseCommands(
                            clientInfo, request, context, operationName, controllerWrapper);
        } else {

            boolean shouldRefresh = !request.isSkipRefresh();
            operationName =
                    shouldRefresh
                            ? "legacy-execute-transfer-and-then-refresh"
                            : "legacy-execute-transfer";
            context =
                    createContentForExecuteTransfer(
                            request, clientInfo, controllerWrapper, correlationId, operationName);
            commands =
                    createTransferBaseCommands(
                            clientInfo, request, context, operationName, controllerWrapper);
            if (shouldRefresh) {
                commands.addAll(
                        createRefreshAccountsCommands(
                                request,
                                context,
                                RefreshableItem.REFRESHABLE_ITEMS_ALL,
                                clientInfo));
                commands.add(
                        new SendAccountSourceInfoEventWorkerCommand(
                                context, accountInformationServiceEventsProducer));
                commands.add(
                        new Psd2PaymentAccountRestrictionWorkerCommand(
                                context,
                                request,
                                regulatoryRestrictions,
                                psd2PaymentAccountClassifier,
                                accountInformationServiceEventsProducer,
                                controllerWrapper));
                commands.add(new DataFetchingRestrictionWorkerCommand(context, controllerWrapper));
                commands.add(new AccountWhitelistRestrictionWorkerCommand(context, request));
                commands.add(new RequestedAccountsRestrictionWorkerCommand(context));
                // SendAccountRestrictionEventsWorkerCommand should be added after all restrictions
                // on accounts have been made
                commands.add(
                        new SendAccountRestrictionEventsWorkerCommand(
                                context, accountInformationServiceEventsProducer));
                commands.addAll(
                        createOrderedRefreshableItemsCommands(
                                request,
                                context,
                                RefreshableItem.REFRESHABLE_ITEMS_ALL,
                                controllerWrapper,
                                clientInfo));
            }
        }

        return new AgentWorkerOperation(
                agentWorkerOperationState, operationName, request, commands, context);
    }

    private AgentWorkerCommandContext createContentForExecuteTransfer(
            TransferRequest request,
            ClientInfo clientInfo,
            ControllerWrapper controllerWrapper,
            String correlationId,
            String operationName) {
        return new AgentWorkerCommandContext(
                request,
                metricRegistry,
                coordinationClient,
                agentsServiceConfiguration,
                aggregatorInfoProvider.createAggregatorInfoFor(clientInfo.getAggregatorId()),
                supplementalInformationController,
                providerSessionCacheController,
                controllerWrapper,
                clientInfo.getClusterId(),
                clientInfo.getAppId(),
                operationName,
                correlationId,
                accountInformationServiceEventsProducer,
                unleashClient,
                requestStatusManager,
                new RawBankDataEventAccumulator());
    }

    public AgentWorkerOperation createOperationExecutePayment(
            TransferRequest request, ClientInfo clientInfo) {

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        boolean shouldRefreshAfterPis = !request.isSkipRefresh();
        String operationName =
                shouldRefreshAfterPis ? "initiate-payment-and-then-refresh" : "initiate-payment";

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
                        operationName,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());

        List<AgentWorkerCommand> commands = new ArrayList<>();

        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(
                        cacheClient, controllerWrapper, cryptoWrapper, metricRegistry);

        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));

        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.getUserAvailability().isUserAvailableForInteraction(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));

        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));

        LockAgentWorkerCommand lockAgentWorkerCommand =
                new LockAgentWorkerCommand(
                        context, operationName, interProcessSemaphoreMutexFactory);

        if (isAisPlusPisFlow(request)) {
            lockAgentWorkerCommand = lockAgentWorkerCommand.withLoginEvent(loginAgentEventProducer);
        }

        commands.add(lockAgentWorkerCommand);

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetInitialAndFinalOperationStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        commands.add(new DecryptCredentialsWorkerCommand(context, credentialsCrypto));

        if (isAisPlusPisFlow(request)) {
            commands.add(
                    new MigrateCredentialsAndAccountsWorkerCommand(
                            context.getRequest(), controllerWrapper, clientInfo));
        }

        if (isAisPlusPisFlow(request)) {
            // Update the status to `UPDATED` if the credential isn't waiting on transactions
            // from the
            // connector and if
            // transactions aren't processed in system. The transaction processing in system
            // will set
            // the status
            // to `UPDATED` when transactions have been processed and new statistics are
            // generated.
            commands.add(
                    new UpdateCredentialsStatusAgentWorkerCommand(
                            controllerWrapper,
                            request.getCredentials(),
                            request.getProvider(),
                            context,
                            c ->
                                    !c.isWaitingOnConnectorTransactions()
                                            && !c.isSystemProcessingTransactions()));
        } else {
            commands.add(
                    new UpdateCredentialsStatusAgentWorkerCommand(
                            controllerWrapper,
                            request.getCredentials(),
                            request.getProvider(),
                            context,
                            c -> true)); // is it enough to return true in this predicate?
        }

        commands.add(
                new ReportProviderMetricsAgentWorkerCommand(
                        context,
                        operationName,
                        reportMetricsAgentWorkerCommandState,
                        new AgentWorkerMetricReporter(
                                metricRegistry, this.providerTierConfiguration)));

        if (!isAisPlusPisFlow(request)) {
            commands.add(
                    new ReportProviderTransferMetricsAgentWorkerCommand(context, operationName));
        }

        commands.add(new CreateCertIdWorkerCommand(context, certificateIdProvider));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient, secretsServiceInternalClient));

        commands.add(new CreateLogMaskerWorkerCommand(context));

        commands.add(new DebugAgentWorkerCommand(context, agentHttpLogsStorageHandler));

        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));

        if (isAisPlusPisFlow(request)) {
            addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
                    commands, context, clientInfo);
            commands.add(
                    new SetCredentialsStatusAgentWorkerCommand(
                            context, CredentialsStatus.UPDATING));
        }

        commands.add(
                new TransferAgentWorkerCommand(
                        context,
                        request,
                        createCommandMetricState(request, clientInfo),
                        exceptionProcessor,
                        paymentExecutionService));

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetImpossibleToAbortRequestStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        if (shouldRefreshAfterPis) {
            commands.addAll(
                    createRefreshAccountsCommands(
                            request, context, RefreshableItem.REFRESHABLE_ITEMS_ALL, clientInfo));
            commands.add(
                    new SendAccountSourceInfoEventWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new Psd2PaymentAccountRestrictionWorkerCommand(
                            context,
                            request,
                            regulatoryRestrictions,
                            psd2PaymentAccountClassifier,
                            accountInformationServiceEventsProducer,
                            controllerWrapper));
            commands.add(new DataFetchingRestrictionWorkerCommand(context, controllerWrapper));
            commands.add(new AccountWhitelistRestrictionWorkerCommand(context, request));
            commands.add(new RequestedAccountsRestrictionWorkerCommand(context));
            // SendAccountRestrictionEventsWorkerCommand should be added after all restrictions on
            // accounts have been made
            commands.add(
                    new SendAccountRestrictionEventsWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.addAll(
                    createOrderedRefreshableItemsCommands(
                            request,
                            context,
                            RefreshableItem.REFRESHABLE_ITEMS_ALL,
                            controllerWrapper,
                            clientInfo));
        }

        return new AgentWorkerOperation(
                agentWorkerOperationState, operationName, request, commands, context);
    }

    @VisibleForTesting
    boolean isAisPlusPisFlow(TransferRequest request) {
        return ProviderBasedValidationsUtil.isDebtorAccountMandatory(
                request.getProvider().getName());
    }

    private boolean isUKOBProvider(Provider provider) {
        return provider.getMarket().equals(MarketCode.GB.toString()) && provider.isOpenBanking();
    }

    private boolean isFrenchTestProvider(Provider provider) {
        return provider.getMarket().equals(MarketCode.FR.toString())
                && provider.getType().isTestProvider();
    }

    public AgentWorkerOperation createOperationExecuteWhitelistedTransfer(
            WhitelistedTransferRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();

        String operationName = "legacy-execute-whitelisted-transfer";

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
                        operationName,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());

        List<AgentWorkerCommand> commands =
                createTransferBaseCommands(
                        clientInfo, request, context, operationName, controllerWrapper);
        commands.addAll(
                createWhitelistRefreshableItemsCommands(
                        request,
                        context,
                        RefreshableItem.REFRESHABLE_ITEMS_ALL,
                        controllerWrapper,
                        clientInfo));

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
                new CredentialsCrypto(
                        cacheClient, controllerWrapper, cryptoWrapper, metricRegistry);
        List<AgentWorkerCommand> commands = Lists.newArrayList();
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.getUserAvailability().isUserAvailableForInteraction(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(
                                context, operationName, interProcessSemaphoreMutexFactory)
                        .withLoginEvent(loginAgentEventProducer));

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetInitialAndFinalOperationStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        commands.add(new DecryptCredentialsWorkerCommand(context, credentialsCrypto));
        commands.add(
                new MigrateCredentialsAndAccountsWorkerCommand(
                        context.getRequest(), controllerWrapper, clientInfo));
        // Update the status to `UPDATED` if the credential isn't waiting on transactions
        // from the
        // connector and if
        // transactions aren't processed in system. The transaction processing in system
        // will set
        // the status
        // to `UPDATED` when transactions have been processed and new statistics are
        // generated.
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
                        context,
                        operationName,
                        reportMetricsAgentWorkerCommandState,
                        new AgentWorkerMetricReporter(
                                metricRegistry, this.providerTierConfiguration)));
        commands.add(
                new SendDataForProcessingAgentWorkerCommand(
                        context,
                        createCommandMetricState(request, clientInfo),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(
                                        RefreshableItem.REFRESHABLE_ITEMS_ALL))));
        commands.add(new CreateCertIdWorkerCommand(context, certificateIdProvider));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient, secretsServiceInternalClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(new DebugAgentWorkerCommand(context, agentHttpLogsStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
                commands, context, clientInfo);
        commands.add(
                new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));
        commands.add(
                new TransferAgentWorkerCommand(
                        context,
                        request,
                        createCommandMetricState(request, clientInfo),
                        exceptionProcessor,
                        paymentExecutionService));

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetImpossibleToAbortRequestStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        return commands;
    }

    List<AgentWorkerCommand> createTransferWithoutRefreshBaseCommands(
            ClientInfo clientInfo,
            TransferRequest request,
            AgentWorkerCommandContext context,
            String operationName,
            ControllerWrapper controllerWrapper) {

        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(
                        cacheClient, controllerWrapper, cryptoWrapper, metricRegistry);

        ArrayList<AgentWorkerCommand> commands = new ArrayList<>();
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.getUserAvailability().isUserAvailableForInteraction(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(
                        context, operationName, interProcessSemaphoreMutexFactory));

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetInitialAndFinalOperationStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        commands.add(new DecryptCredentialsWorkerCommand(context, credentialsCrypto));
        commands.add(
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        request.getCredentials(),
                        request.getProvider(),
                        context,
                        c -> true)); // is it enough to return true in this predicate?
        commands.add(
                new ReportProviderMetricsAgentWorkerCommand(
                        context,
                        operationName,
                        reportMetricsAgentWorkerCommandState,
                        new AgentWorkerMetricReporter(
                                metricRegistry, this.providerTierConfiguration)));
        commands.add(new ReportProviderTransferMetricsAgentWorkerCommand(context, operationName));

        commands.add(new CreateCertIdWorkerCommand(context, certificateIdProvider));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient, secretsServiceInternalClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(new DebugAgentWorkerCommand(context, agentHttpLogsStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        commands.add(
                new TransferAgentWorkerCommand(
                        context,
                        request,
                        createCommandMetricState(request, clientInfo),
                        exceptionProcessor,
                        paymentExecutionService));

        if (isSupplementalInformationWaitingAbortFeatureEnabled(clientInfo.getAppId(), request)) {
            commands.add(
                    new SetImpossibleToAbortRequestStatusAgentWorkerCommand(
                            context.getRequest().getCredentials().getId(), requestStatusManager));
        }

        return commands;
    }

    public AgentWorkerOperation createOperationCreateCredentials(
            CredentialsRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();
        String operation = "create-credentials";

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
                        operation,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(
                        cacheClient, controllerWrapper, cryptoWrapper, metricRegistry);

        List<AgentWorkerCommand> commands = Lists.newArrayList();
        commands.add(new ClearSensitiveInformationCommand(context));
        commands.add(new EncryptCredentialsWorkerCommand(context, false, credentialsCrypto));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operation, request, commands, context);
    }

    public AgentWorkerOperation createOperationUpdate(
            CredentialsRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();
        String operation = "update-credentials";

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
                        operation,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(
                        cacheClient, controllerWrapper, cryptoWrapper, metricRegistry);
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(new ClearSensitiveInformationCommand(context));
        // acquire lock to avoid encryption/decryption race conditions
        commands.add(
                new LockAgentWorkerCommand(context, operation, interProcessSemaphoreMutexFactory));
        commands.add(new EncryptCredentialsWorkerCommand(context, false, credentialsCrypto));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operation, request, commands, context);
    }

    public AgentWorkerOperation createOperationReEncryptCredentials(
            ReEncryptCredentialsRequest request, ClientInfo clientInfo) {
        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = UUIDUtils.generateUUID();
        String operation = "reencrypt-credentials";

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
                        operation,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());

        ImmutableList<AgentWorkerCommand> commands =
                ImmutableList.of(
                        new LockAgentWorkerCommand(
                                context, operation, interProcessSemaphoreMutexFactory),
                        new DecryptCredentialsWorkerCommand(
                                context,
                                new CredentialsCrypto(
                                        cacheClient,
                                        controllerWrapper,
                                        cryptoWrapper,
                                        metricRegistry)),
                        new EncryptCredentialsWorkerCommand(
                                context,
                                new CredentialsCrypto(
                                        cacheClient,
                                        controllerWrapper,
                                        cryptoWrapper,
                                        metricRegistry)));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operation, request, commands, context);
    }

    // for each account type,
    List<AgentWorkerCommand> createRefreshAccountsCommands(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh,
            ClientInfo clientInfo) {

        List<RefreshableItem> items = RefreshableItem.sort(convertLegacyItems(itemsToRefresh));

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        for (RefreshableItem item : items) {
            if (RefreshableItem.isAccount(item)) {
                commands.add(
                        new RefreshItemAgentWorkerCommand(
                                context,
                                item,
                                createCommandMetricState(request, clientInfo),
                                refreshEventProducer));
            }
        }

        commands.add(
                new TransactionRefreshScopeFilteringCommand(
                        context.getAccountDataCache(), request));

        return commands;
    }

    /** Use this operation when refreshing only the accounts that are available in the request. */
    public AgentWorkerOperation createOperationWhitelistRefresh(
            RefreshWhitelistInformationRequest request, ClientInfo clientInfo) {
        if (Objects.isNull(request.getItemsToRefresh()) || request.getItemsToRefresh().isEmpty()) {
            // Add all available items if none were submitted.
            // Todo: Remove this once it has been verified that no consumer sends in an empty/null
            // list.
            // Instead it should abort if it's empty (empty list == do nothing).
            log.info("createOperationWhitelistRefresh called with empty or null itemsToRefresh.");
            request.setItemsToRefresh(RefreshableItem.REFRESHABLE_ITEMS_ALL);
        }

        log.debug("Creating whitelist refresh operation chain for credential");

        ControllerWrapper controllerWrapper =
                controllerWrapperProvider.createControllerWrapper(clientInfo.getClusterId());

        final String correlationId = generateOrGetCorrelationId(request.getRefreshId());
        String metricsName = (request.isUserPresent() ? "refresh-manual" : "refresh-auto");

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
                        metricsName,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());
        CryptoWrapper cryptoWrapper =
                cryptoConfigurationDao.getCryptoWrapperOfClientName(clientInfo.getClientName());
        CredentialsCrypto credentialsCrypto =
                new CredentialsCrypto(
                        cacheClient, controllerWrapper, cryptoWrapper, metricRegistry);

        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(
                new RefreshCommandChainEventTriggerCommand(
                        credentialsEventProducer,
                        context.getCorrelationId(),
                        request.getCredentials(),
                        clientInfo.getAppId(),
                        request.getItemsToRefresh(),
                        request.isUserPresent(),
                        clientInfo.getClusterId()));
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.getUserAvailability().isUserAvailableForInteraction(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(context, metricsName, interProcessSemaphoreMutexFactory)
                        .withLoginEvent(loginAgentEventProducer));
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
                        context,
                        metricsName,
                        reportMetricsAgentWorkerCommandState,
                        new AgentWorkerMetricReporter(
                                metricRegistry, this.providerTierConfiguration)));
        commands.add(
                new SendDataForProcessingAgentWorkerCommand(
                        context,
                        createCommandMetricState(request, clientInfo),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(request.getItemsToRefresh()))));
        commands.add(new CreateCertIdWorkerCommand(context, certificateIdProvider));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient, secretsServiceInternalClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(new DebugAgentWorkerCommand(context, agentHttpLogsStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
                commands, context, clientInfo);
        commands.add(
                new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));
        commands.addAll(
                createWhitelistRefreshableItemsCommands(
                        request,
                        context,
                        request.getItemsToRefresh(),
                        controllerWrapper,
                        clientInfo));
        commands.add(new LogRefreshSummaryAgentWorkerCommand(context));

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
                        operationMetricName,
                        correlationId,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager,
                        new RawBankDataEventAccumulator());
        List<AgentWorkerCommand> commands = Lists.newArrayList();

        commands.add(
                new RefreshCommandChainEventTriggerCommand(
                        credentialsEventProducer,
                        context.getCorrelationId(),
                        request.getCredentials(),
                        clientInfo.getAppId(),
                        request.getItemsToRefresh(),
                        request.isUserPresent(),
                        clientInfo.getClusterId()));
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.getUserAvailability().isUserAvailableForInteraction(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(
                                context, operationMetricName, interProcessSemaphoreMutexFactory)
                        .withLoginEvent(loginAgentEventProducer));
        commands.add(
                new DecryptCredentialsWorkerCommand(
                        context,
                        new CredentialsCrypto(
                                cacheClient, controllerWrapper, cryptoWrapper, metricRegistry)));
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
                        context,
                        operationMetricName,
                        reportMetricsAgentWorkerCommandState,
                        new AgentWorkerMetricReporter(
                                metricRegistry, this.providerTierConfiguration)));
        commands.add(
                new SendDataForProcessingAgentWorkerCommand(
                        context,
                        createCommandMetricState(request, clientInfo),
                        ProcessableItem.fromRefreshableItems(
                                RefreshableItem.convertLegacyItems(request.getItemsToRefresh()))));
        commands.add(new CreateCertIdWorkerCommand(context, certificateIdProvider));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient, secretsServiceInternalClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(new DebugAgentWorkerCommand(context, agentHttpLogsStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));
        addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
                commands, context, clientInfo);

        commands.addAll(
                createWhitelistRefreshableItemsCommands(
                        request,
                        context,
                        request.getItemsToRefresh(),
                        controllerWrapper,
                        clientInfo));

        return new AgentWorkerOperation(
                agentWorkerOperationState, operationMetricName, request, commands, context);
    }

    private ImmutableList<AgentWorkerCommand> createWhitelistRefreshableItemsCommands(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh,
            ControllerWrapper controllerWrapper,
            ClientInfo clientInfo) {

        if (isBalanceCalculationEnabled(context)) {
            log.debug("[BALANCE CALCULATOR] Enabled");
            return createWhitelistRefreshableItemsCommandsWithChanges(
                    request, context, itemsToRefresh, controllerWrapper, clientInfo);
        }

        log.debug("[BALANCE CALCULATOR] Disabled");
        return createWhitelistRefreshableItemsCommandsWithoutChanges(
                request, context, itemsToRefresh, controllerWrapper, clientInfo);
    }

    private ImmutableList<AgentWorkerCommand> createWhitelistRefreshableItemsCommandsWithChanges(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh,
            ControllerWrapper controllerWrapper,
            ClientInfo clientInfo) {
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
            commands.addAll(
                    createRefreshAccountsCommands(request, context, accountItems, clientInfo));

            commands.add(
                    new SendAccountSourceInfoEventWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new Psd2PaymentAccountRestrictionWorkerCommand(
                            context,
                            request,
                            regulatoryRestrictions,
                            psd2PaymentAccountClassifier,
                            accountInformationServiceEventsProducer,
                            controllerWrapper));
            commands.add(new DataFetchingRestrictionWorkerCommand(context, controllerWrapper));
            commands.add(new AccountSegmentRestrictionWorkerCommand(context));
            commands.add(new RequestedAccountsRestrictionWorkerCommand(context));
            // If this is an optIn request we request the caller do supply supplemental information
            // with the
            // accounts they want to whitelist.
            if (request instanceof ConfigureWhitelistInformationRequest) {
                commands.add(
                        new RequestUserOptInAccountsAgentWorkerCommand(
                                context,
                                (ConfigureWhitelistInformationRequest) request,
                                controllerWrapper,
                                loginAgentEventProducer));
                commands.add(
                        new SetCredentialsStatusAgentWorkerCommand(
                                context, CredentialsStatus.UPDATING));
            }

            // Update the accounts on system side
            commands.add(new AccountWhitelistRestrictionWorkerCommand(context, request));
            // SendAccountRestrictionEventsWorkerCommand should be added after all restrictions on
            // accounts have been made
            commands.add(
                    new SendAccountRestrictionEventsWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            psd2PaymentAccountClassifier,
                            controllerWrapper,
                            false));

            /* Special command; see {@link AbnAmroSpecificCase} for more information. */
            if (Objects.equals("abnamro.AbnAmroAgent", request.getProvider().getClassName())
                    && Objects.equals("nl-abnamro", request.getProvider().getName())) {
                commands.add(new AbnAmroSpecificCase(context));
            }
        }

        List<RefreshableItem> transactionItems =
                items.stream().filter(RefreshableItem::isTransaction).collect(Collectors.toList());

        transactionItems.forEach(
                item ->
                        commands.add(
                                new RefreshItemAgentWorkerCommand(
                                        context,
                                        item,
                                        createCommandMetricState(request, clientInfo),
                                        refreshEventProducer)));

        commands.add(
                new RefreshPostProcessingAgentWorkerCommand(
                        context, createCommandMetricState(request, clientInfo)));

        commands.add(
                new SendAccountsToUpdateServiceAgentWorkerCommand(
                        context, createCommandMetricState(request, clientInfo)));

        // Add refresh commands for all items that aren't accounts nor transactions
        items.stream()
                .filter(i -> !accountItems.contains(i) && !transactionItems.contains(i))
                .forEach(
                        item ->
                                commands.add(
                                        new RefreshItemAgentWorkerCommand(
                                                context,
                                                item,
                                                createCommandMetricState(request, clientInfo),
                                                refreshEventProducer)));

        commands.add(
                new TransactionRefreshScopeFilteringCommand(
                        context.getAccountDataCache(), request));

        if (accountItems.size() > 0) {
            commands.add(
                    new EmitEventsAfterRefreshAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            dataTrackerEventProducer,
                            accountHolderRefreshedEventProducer,
                            items,
                            eventSender));
        }

        // === END REFRESHING ===
        return commands.build();
    }

    private ImmutableList<AgentWorkerCommand> createWhitelistRefreshableItemsCommandsWithoutChanges(
            CredentialsRequest request,
            AgentWorkerCommandContext context,
            Set<RefreshableItem> itemsToRefresh,
            ControllerWrapper controllerWrapper,
            ClientInfo clientInfo) {
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
            commands.addAll(
                    createRefreshAccountsCommands(request, context, accountItems, clientInfo));

            commands.add(
                    new SendAccountSourceInfoEventWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new Psd2PaymentAccountRestrictionWorkerCommand(
                            context,
                            request,
                            regulatoryRestrictions,
                            psd2PaymentAccountClassifier,
                            accountInformationServiceEventsProducer,
                            controllerWrapper));
            commands.add(new DataFetchingRestrictionWorkerCommand(context, controllerWrapper));
            commands.add(new AccountSegmentRestrictionWorkerCommand(context));
            commands.add(new RequestedAccountsRestrictionWorkerCommand(context));
            // If this is an optIn request we request the caller do supply supplemental information
            // with the
            // accounts they want to whitelist.
            if (request instanceof ConfigureWhitelistInformationRequest) {
                commands.add(
                        new RequestUserOptInAccountsAgentWorkerCommand(
                                context,
                                (ConfigureWhitelistInformationRequest) request,
                                controllerWrapper,
                                loginAgentEventProducer));
                commands.add(
                        new SetCredentialsStatusAgentWorkerCommand(
                                context, CredentialsStatus.UPDATING));
            }

            // Update the accounts on system side
            commands.add(new AccountWhitelistRestrictionWorkerCommand(context, request));
            // SendAccountRestrictionEventsWorkerCommand should be added after all restrictions on
            // accounts have been made
            commands.add(
                    new SendAccountRestrictionEventsWorkerCommand(
                            context, accountInformationServiceEventsProducer));
            commands.add(
                    new SendAccountsToUpdateServiceAgentWorkerCommand(
                            context, createCommandMetricState(request, clientInfo)));
            commands.add(
                    new SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            psd2PaymentAccountClassifier,
                            controllerWrapper,
                            false));

            /* Special command; see {@link AbnAmroSpecificCase} for more information. */
            if (Objects.equals("abnamro.AbnAmroAgent", request.getProvider().getClassName())
                    && Objects.equals("nl-abnamro", request.getProvider().getName())) {
                commands.add(new AbnAmroSpecificCase(context));
            }
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
                                                createCommandMetricState(request, clientInfo),
                                                refreshEventProducer)));

        commands.add(
                new TransactionRefreshScopeFilteringCommand(
                        context.getAccountDataCache(), request));

        if (accountItems.size() > 0) {
            commands.add(
                    new EmitEventsAfterRefreshAgentWorkerCommand(
                            context,
                            createCommandMetricState(request, clientInfo),
                            dataTrackerEventProducer,
                            accountHolderRefreshedEventProducer,
                            items,
                            eventSender));
        }

        // === END REFRESHING ===
        return commands.build();
    }

    public Optional<AgentWorkerOperation> createOperationCreateBeneficiary(
            CreateBeneficiaryCredentialsRequest request, ClientInfo clientInfo) {
        // Check if this feature is enabled.
        if (!agentsServiceConfiguration.isFeatureEnabled("createBeneficiary")) {
            return Optional.empty();
        }
        return Optional.of(
                CreateBeneficiaryAgentWorkerCommandOperation.createOperationCreateBeneficiary(
                        request,
                        clientInfo,
                        metricRegistry,
                        coordinationClient,
                        controllerWrapperProvider.createControllerWrapper(
                                clientInfo.getClusterId()),
                        agentsServiceConfiguration,
                        aggregatorInfoProvider.createAggregatorInfoFor(
                                clientInfo.getAggregatorId()),
                        supplementalInformationController,
                        providerSessionCacheController,
                        generateOrGetCorrelationId(request.getRefreshId()),
                        cryptoConfigurationDao.getCryptoWrapperOfClientName(
                                clientInfo.getClientName()),
                        circuitBreakAgentWorkerCommandState,
                        interProcessSemaphoreMutexFactory,
                        cacheClient,
                        reportMetricsAgentWorkerCommandState,
                        tppSecretsServiceClient,
                        secretsServiceInternalClient,
                        agentHttpLogsStorageHandler,
                        instantiateAgentWorkerCommandState,
                        loginAgentWorkerCommandState,
                        loginAgentEventProducer,
                        agentWorkerOperationState,
                        this.providerTierConfiguration,
                        accountInformationServiceEventsProducer,
                        unleashClient,
                        requestStatusManager));
    }

    private static String generateOrGetCorrelationId(String correlationId) {
        if (correlationId == null) {
            return UUIDUtils.generateUUID();
        }
        return correlationId;
    }

    private void addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
            List<AgentWorkerCommand> commands,
            AgentWorkerCommandContext context,
            ClientInfo clientInfo) {

        /* LoginAgentWorkerCommand needs to always be used together with ClearSensitivePayloadOnForceAuthenticateCommand */

        commands.add(new ClearSensitivePayloadOnForceAuthenticateCommand(context));
        commands.add(
                new LoginAgentWorkerCommand(
                        context,
                        loginAgentWorkerCommandState,
                        createCommandMetricState(context.getRequest(), clientInfo),
                        metricRegistry,
                        loginAgentEventProducer));
    }

    private boolean isSupplementalInformationWaitingAbortFeatureEnabled(
            String appId, CredentialsRequest request) {
        String credentialsId = request.getCredentials().getId();
        boolean isUserPresent = request.getUserAvailability().isUserPresent();
        return isUserPresent
                && unleashClient.isToggleEnabled(
                        Toggle.of("supplemental-information-waiting-abort")
                                .context(
                                        UnleashContext.builder()
                                                .userId(appId)
                                                .sessionId(credentialsId)
                                                .build())
                                .build());
    }

    private boolean isBalanceCalculationEnabled(AgentWorkerCommandContext context) {
        boolean balanceCalculationEnabled;
        try {
            String appId = context.getAppId();
            String providerId = context.getProviderId();
            String credentialsId = context.getRequest().getCredentials().getId();

            Toggle toggle =
                    Toggle.of("uk-balance-calculators")
                            .context(
                                    UnleashContext.builder()
                                            .sessionId(credentialsId)
                                            .addProperty(
                                                    Constants.Context.PROVIDER_NAME.getValue(),
                                                    providerId)
                                            .addProperty(Constants.Context.APP_ID.getValue(), appId)
                                            .build())
                            .build();
            balanceCalculationEnabled = unleashClient.isToggleEnabled(toggle);
        } catch (Exception e) {
            log.warn("[BALANCE CALCULATOR] Failed to fetch balance calculator toggle status");
            balanceCalculationEnabled = false;
        }

        return balanceCalculationEnabled;
    }
}
