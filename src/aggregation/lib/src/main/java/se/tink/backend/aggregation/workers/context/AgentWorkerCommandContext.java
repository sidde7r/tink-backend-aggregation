package se.tink.backend.aggregation.workers.context;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.agents.SetAccountsToAggregateContext;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.utils.mappers.CoreAccountMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.compliance.regulatory_restrictions.RegulatoryRestrictions;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.workers.operation.AgentWorkerContext;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class AgentWorkerCommandContext extends AgentWorkerContext
        implements SetAccountsToAggregateContext {
    private static final Logger log = LoggerFactory.getLogger(AgentWorkerCommandContext.class);
    protected CuratorFramework coordinationClient;
    private static final String EMPTY_CLASS_NAME = "";

    protected Agent agent;

    protected final Counter refreshTotal;
    protected final MetricId.MetricLabels defaultMetricLabels;

    protected static final Set<AccountTypes> TARGET_ACCOUNT_TYPES =
            new HashSet<>(
                    Arrays.asList(
                            AccountTypes.CHECKING,
                            AccountTypes.SAVINGS,
                            AccountTypes.CREDIT_CARD,
                            AccountTypes.LOAN,
                            AccountTypes.INVESTMENT));

    protected long timeLeavingQueue;
    protected long timePutOnQueue;
    protected AgentsServiceConfiguration agentsServiceConfiguration;
    protected List<String> uniqueIdOfUserSelectedAccounts;

    public AgentWorkerCommandContext(
            CredentialsRequest request,
            MetricRegistry metricRegistry,
            CuratorFramework coordinationClient,
            AgentsServiceConfiguration agentsServiceConfiguration,
            AggregatorInfo aggregatorInfo,
            SupplementalInformationController supplementalInformationController,
            ProviderSessionCacheController providerSessionCacheController,
            ControllerWrapper controllerWrapper,
            String clusterId,
            String appId,
            String correlationId,
            RegulatoryRestrictions regulatoryRestrictions,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer) {
        super(
                request,
                metricRegistry,
                coordinationClient,
                aggregatorInfo,
                supplementalInformationController,
                providerSessionCacheController,
                controllerWrapper,
                clusterId,
                appId,
                correlationId,
                regulatoryRestrictions,
                accountInformationServiceEventsProducer);
        this.coordinationClient = coordinationClient;
        this.timePutOnQueue = System.currentTimeMillis();
        this.uniqueIdOfUserSelectedAccounts = Lists.newArrayList();

        Provider provider = request.getProvider();

        defaultMetricLabels =
                new MetricId.MetricLabels()
                        .addAll(
                                createClusterMetricsLabels(
                                        controllerWrapper.getHostConfiguration().getClusterId()))
                        .add("provider", provider.getName())
                        .add("market", provider.getMarket())
                        .add(
                                "agent",
                                Optional.ofNullable(provider.getClassName())
                                        .orElse(EMPTY_CLASS_NAME))
                        .add("request_type", request.getType().name());

        refreshTotal =
                metricRegistry.meter(MetricId.newId("accounts_refresh").label(defaultMetricLabels));

        this.agentsServiceConfiguration = agentsServiceConfiguration;
    }

    // TODO: We should do this some other way. This is a hack we can use for now.
    private MetricId.MetricLabels createClusterMetricsLabels(String clusterId) {
        List<String> splitClusterId = Splitter.on("-").splitToList(clusterId);

        if (splitClusterId.size() != 2) {
            // The clusterId should be of the format <cluster name>-<environment>, i.e.
            // oxford-staging
            log.warn("SplitClusterId did not have size of exactly 2. ClusterId: {}", clusterId);
            return MetricId.MetricLabels.createEmpty();
        }

        return new MetricId.MetricLabels()
                .add("request_cluster", splitClusterId.get(0))
                .add("request_environment", splitClusterId.get(1));
    }

    public void processAccounts() {
        Credentials credentials = request.getCredentials();

        if (credentials.getStatus() != CredentialsStatus.UPDATING) {
            log.warn(
                    String.format(
                            "Status does not warrant account processing: %s",
                            credentials.getStatus()));
            return;
        }

        // Metrics
        refreshTotal.inc();

        List<Account> allCollectedAccounts = getAccountDataCache().getAllAccounts();
        TARGET_ACCOUNT_TYPES.stream()
                .filter(
                        accountType ->
                                allCollectedAccounts.stream()
                                        .noneMatch(account -> account.getType() == accountType))
                .forEach(
                        accountType ->
                                getMetricRegistry()
                                        .meter(
                                                MetricId.newId("no_accounts_fetched")
                                                        .label(defaultMetricLabels)
                                                        .label("type", accountType.toString()))
                                        .inc());

        // Requires Accounts in list to have been "updated" towards System's UpdateService to get
        // their real stored id
        List<String> accountIds =
                getAccountDataCache().getProcessedAccounts().stream()
                        .map(Account::getId)
                        .collect(Collectors.toList());

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest
                processAccountsRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .ProcessAccountsRequest();
        processAccountsRequest.setAccountIds(accountIds);
        processAccountsRequest.setCredentialsId(credentials.getId());
        processAccountsRequest.setCredentialsDataVersion(credentials.getDataVersion());
        processAccountsRequest.setUserId(request.getUser().getId());

        controllerWrapper.processAccounts(processAccountsRequest);
    }

    public CuratorFramework getCoordinationClient() {
        return coordinationClient;
    }

    public long getTimeLeavingQueue() {
        return timeLeavingQueue;
    }

    public void setTimeLeavingQueue(long timeLeavingQueue) {
        this.timeLeavingQueue = timeLeavingQueue;
    }

    public long getTimePutOnQueue() {
        return timePutOnQueue;
    }

    public void addEventListener(AgentEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void removeEventListener(AgentEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    public boolean isSystemProcessingTransactions() {
        return isSystemProcessingTransactions;
    }

    public void sendAllCachedAccountsToUpdateService() {
        getAccountDataCache()
                .getFilteredAccounts()
                .forEach(
                        filteredAccount -> sendAccountToUpdateService(filteredAccount.getBankId()));
    }

    public void sendAllCachedAccountsHoldersToUpdateService() {
        getAccountDataCache()
                .getProcessedAccounts()
                .forEach(
                        account -> {
                            AccountHolder accountHolder = sendAccountHolderToUpdateService(account);
                            sendAccountHoldersRefreshedEvent(account.getId(), accountHolder);
                        });
    }

    private void sendAccountHoldersRefreshedEvent(
            String tinkAccountId, AccountHolder accountHolder) {
        accountInformationServiceEventsProducer.sendAccountHoldersRefreshedEvent(
                getClusterId(),
                getAppId(),
                getRequest().getUser().getId(),
                getRequest().getProvider().getName(),
                correlationId,
                tinkAccountId,
                Optional.ofNullable(accountHolder)
                        .map(AccountHolder::getType)
                        .map(AccountHolderType::name)
                        .orElse(null),
                Optional.ofNullable(accountHolder)
                        .map(AccountHolder::getIdentities)
                        .map(Collection::size)
                        .orElse(0));
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;

        if (agent instanceof AgentEventListener) {
            eventListeners.add((AgentEventListener) agent);
        }
    }

    @Override
    public void setAccountsToAggregate(List<Account> accounts) {}

    @Override
    public List<Account> getCachedAccounts() {
        return new ArrayList<>(getAccountDataCache().getFilteredAccounts());
    }

    public List<Pair<Account, AccountFeatures>> getCachedAccountsWithFeatures() {
        return getAccountDataCache().getFilteredAccountData().stream()
                .map(
                        accountData ->
                                new Pair<>(
                                        accountData.getAccount(), accountData.getAccountFeatures()))
                .collect(Collectors.toList());
    }

    public IdentityData getCachedIdentityData() {
        return identityData;
    }

    @Override
    public List<String> getUniqueIdOfUserSelectedAccounts() {
        return uniqueIdOfUserSelectedAccounts;
    }

    public void addOptInAccountUniqueId(List<String> optInAccountUniqueId) {
        this.uniqueIdOfUserSelectedAccounts = optInAccountUniqueId;
    }

    public boolean isWhitelistRefresh() {
        return isWhitelistRefresh;
    }

    public void setWhitelistRefresh(boolean whitelistRefresh) {
        isWhitelistRefresh = whitelistRefresh;
    }

    public AgentsServiceConfiguration getAgentsServiceConfiguration() {
        return agentsServiceConfiguration;
    }

    public void updateSignableOperation(SignableOperation signableOperation) {
        controllerWrapper.updateSignableOperation(signableOperation);
    }

    public void updateSignableOperationStatus(
            SignableOperation signableOperation,
            SignableOperationStatuses status,
            String statusMessage) {
        signableOperation.setStatus(status);
        if (Optional.ofNullable(getCatalog()).isPresent() && statusMessage != null) {
            signableOperation.setStatusMessage(getCatalog().getString(statusMessage));
        } else {
            signableOperation.setStatusMessage(statusMessage);
        }

        updateSignableOperation(signableOperation);
    }

    public void updateSignableOperationStatus(
            SignableOperation signableOperation,
            SignableOperationStatuses status,
            String statusMessage,
            String internalStatus) {
        signableOperation.setStatus(status);
        signableOperation.setInternalStatus(internalStatus);
        if (Optional.ofNullable(getCatalog()).isPresent() && statusMessage != null) {
            signableOperation.setStatusMessage(getCatalog().getString(statusMessage));
        } else {
            signableOperation.setStatusMessage(statusMessage);
        }

        updateSignableOperation(signableOperation);
    }

    public void processTransferDestinationPatterns() {
        Map<Account, List<TransferDestinationPattern>> transferDestinationPatterns =
                getAccountDataCache().getTransferDestinationPatternsToBeProcessed();
        if (transferDestinationPatterns.isEmpty()) {
            // Nothing to process.
            return;
        }

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                        .UpdateTransferDestinationPatternsRequest
                updateRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .UpdateTransferDestinationPatternsRequest();

        updateRequest.setDestinationsBySouce(destinationBySource(transferDestinationPatterns));
        updateRequest.setUserId(request.getUser().getId());
        updateRequest.setCredentialsId(request.getCredentials().getId());
        updateRequest.setCredentialsDataVersion(request.getCredentials().getDataVersion());

        controllerWrapper.updateTransferDestinationPatterns(updateRequest);
    }

    private Map<se.tink.libraries.account.rpc.Account, List<TransferDestinationPattern>>
            destinationBySource(
                    Map<Account, List<TransferDestinationPattern>>
                            transferDestinationPatternsByAccount) {
        return transferDestinationPatternsByAccount.entrySet().stream()
                .map(
                        e ->
                                new AbstractMap.SimpleEntry<>(
                                        CoreAccountMapper.fromAggregation(e.getKey()),
                                        e.getValue()))
                .collect(
                        Collectors.toMap(
                                AbstractMap.SimpleEntry::getKey,
                                AbstractMap.SimpleEntry::getValue));
    }

    public void processEinvoices() {
        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest
                updateTransfersRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .UpdateTransfersRequest();
        updateTransfersRequest.setUserId(request.getUser().getId());
        updateTransfersRequest.setCredentialsId(request.getCredentials().getId());
        updateTransfersRequest.setCredentialsDataVersion(request.getCredentials().getDataVersion());
        updateTransfersRequest.setTransfers(transfers);

        controllerWrapper.processEinvoices(updateTransfersRequest);
    }
}
