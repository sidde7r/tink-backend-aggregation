package se.tink.backend.aggregation.workers;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.agents.SetAccountsToAggregateContext;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.agents.utils.mappers.CoreAccountMapper;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.libraries.metrics.utils.MetricsUtils;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class AgentWorkerCommandContext extends AgentWorkerContext implements SetAccountsToAggregateContext {
    private static final Logger log = LoggerFactory.getLogger(AgentWorkerCommandContext.class);
    protected CuratorFramework coordinationClient;
    private static final String EMPTY_CLASS_NAME = "";

    protected Agent agent;

    protected final Counter refreshTotal;
    protected final Counter inconsistencyBetweelAccountsTotal;
    protected final Counter zeroAccountsFoundDuringRefreshTotal;
    protected final Counter accountsNotBeingSentToSystemTotal;
    protected final MetricId.MetricLabels defaultMetricLabels;

    protected static final Set<AccountTypes> TARGET_ACCOUNT_TYPES = new HashSet<>(Arrays.asList(
            AccountTypes.CHECKING,
            AccountTypes.SAVINGS,
            AccountTypes.CREDIT_CARD,
            AccountTypes.LOAN,
            AccountTypes.INVESTMENT));

    protected long timeLeavingQueue;
    protected long timePutOnQueue;
    protected AgentsServiceConfiguration agentsServiceConfiguration;
    protected List<String> uniqueIdOfUserSelectedAccounts;


    public AgentWorkerCommandContext(CredentialsRequest request,
                                     MetricRegistry metricRegistry,
                                     CuratorFramework coordinationClient,
                                     AgentsServiceConfiguration agentsServiceConfiguration,
                                     AggregatorInfo aggregatorInfo,
                                     SupplementalInformationController supplementalInformationController,
                                     ControllerWrapper controllerWrapper,
                                     String clusterId) {
        super(request, metricRegistry, coordinationClient, aggregatorInfo, supplementalInformationController,
                controllerWrapper, clusterId);
        this.coordinationClient = coordinationClient;
        this.timePutOnQueue = System.currentTimeMillis();
        this.uniqueIdOfUserSelectedAccounts = Lists.newArrayList();

        Provider provider = request.getProvider();

        defaultMetricLabels = new MetricId.MetricLabels()
                .addAll(createClusterMetricsLabels(controllerWrapper.getHostConfiguration().getClusterId()))
                .add("provider", MetricsUtils.cleanMetricName(provider.getName()))
                .add("market", provider.getMarket())
                .add("agent", Optional.ofNullable(provider.getClassName()).orElse(EMPTY_CLASS_NAME))
                .add("request_type", request.getType().name());

        refreshTotal = metricRegistry.meter(
                MetricId.newId("accounts_refresh")
                        .label(defaultMetricLabels));

        inconsistencyBetweelAccountsTotal = metricRegistry.meter(
                MetricId.newId("inconsistency_between_accounts")
                        .label(defaultMetricLabels));

        zeroAccountsFoundDuringRefreshTotal = metricRegistry.meter(
                MetricId.newId("zero_accounts_found_during_refresh")
                        .label(defaultMetricLabels));

        accountsNotBeingSentToSystemTotal = metricRegistry.meter(
                MetricId.newId("accounts_not_being_sent_to_system")
                        .label(defaultMetricLabels));

        this.agentsServiceConfiguration = agentsServiceConfiguration;
    }

    // TODO: We should do this some other way. This is a hack we can use for now.
    private MetricId.MetricLabels createClusterMetricsLabels(String clusterId) {
        List<String> splitClusterId = Splitter.on("-").splitToList(clusterId);

        if (splitClusterId.size() != 2) {
            // The clusterId should be of the format <cluster name>-<environment>, i.e. oxford-staging
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
            log.warn(String.format("Status does not warrant account processing: %s", credentials.getStatus()));
            return;
        }

        // Metrics
        refreshTotal.inc();
        TARGET_ACCOUNT_TYPES.forEach(accountType -> {
            if (allAvailableAccountsByUniqueId.values().stream()
                    .noneMatch(pair -> pair.first.getType() == accountType)) {
                getMetricRegistry().meter(
                        MetricId.newId("no_accounts_fetched")
                                .label(defaultMetricLabels)
                                .label("type", accountType.toString())
                ).inc();
            }
        });

        // Requires Accounts in list to have been "updated" towards System's UpdateService to get their real stored id
        List<String> accountIds = Lists.newArrayList(updatedAccountsByTinkId.keySet());

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest processAccountsRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest();
        processAccountsRequest.setAccountIds(accountIds);
        processAccountsRequest.setCredentialsId(credentials.getId());
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

        compareAccountsBeforeAndAfterUpdate();

        for (String uniqueId : allAvailableAccountsByUniqueId.keySet()) {
            sendAccountToUpdateService(uniqueId);
        }
    }

    private void compareAccountsBeforeAndAfterUpdate() {
        List<Account> accountsBeforeRefresh = request.getAccounts();
        List<Account> accountsFoundByAgent = allAvailableAccountsByUniqueId
                .values()
                .stream()
                .map(p -> p.first)
                .collect(Collectors.toList());

        // If it was 0 before and 0 found something might be wrong (maybe not though)
        // If it's 0 accounts before, it means that we are **probably** trying to refresh this credentials for the first time (but not 100% of the time).
        if (accountsBeforeRefresh.size() == 0 && accountsFoundByAgent.size() == 0) {
            zeroAccountsFoundDuringRefreshTotal.inc();
            return;
        }

        if (!(request instanceof RefreshInformationRequest)) {
            // If it's not a refresh it shouldn't get here, but good to return anyway
            return;
        }

        // If the number of accounts sent to system are different than the accounts that we received in the request,
        //      that might mean that the user has a new account in the credentials. (not a problem)
        //      that an account on the credentials closed. (not a problem)
        // But it's  not something that we expect happening on multiple users at the same time. (problem)
        if (accountsFoundByAgent.size() != accountsBeforeRefresh.size()) {
            inconsistencyBetweelAccountsTotal.inc();
            return;
        }

        List<String> accountIdsBeforeRefresh = accountsBeforeRefresh.stream().map(Account::getId).collect(Collectors.toList());
        List<String> accountIdsFoundByAgent  = accountsFoundByAgent.stream().map(Account::getId).collect(Collectors.toList());

        for (String idBeforeRefresh : accountIdsBeforeRefresh) {
            if (!accountIdsFoundByAgent.contains(idBeforeRefresh)) {

                // An account that existed before is not getting sent to system. This happen happen when
                //      the user closed an account on their credentials. (not a problem)
                //      the client is not refreshing this RefreshableItem. (not a problem)
                // But it's not something that we expect happening on multiple users at the same time. (problem)
                accountsNotBeingSentToSystemTotal.inc();
                log.warn(String.format("accountid %s is missing from the ones sent to system.", idBeforeRefresh));
            }
        }

        // TODO: Have 3 different metrics for ids, COMPLETE_MATCH, PARTIAL_MISMATCH, COMPLETE_MISMATCH, increment accordingly
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
    public void setAccountsToAggregate(List<Account> accounts) {
        accountsToAggregate = accounts;
    }

    @Override
    public List<Account> getCachedAccounts() {
        return allAvailableAccountsByUniqueId.values().stream().map(p -> p.first).collect(Collectors.toList());
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

    public void updateSignableOperationStatus(SignableOperation signableOperation, SignableOperationStatuses status) {
        signableOperation.setStatus(status);
        signableOperation.setStatusMessage(null);

        updateSignableOperation(signableOperation);
    }

    public void updateSignableOperationStatus(SignableOperation signableOperation, SignableOperationStatuses status,
            String statusMessage) {
        signableOperation.setStatus(status);
        signableOperation.setStatusMessage(statusMessage);

        updateSignableOperation(signableOperation);
    }

    public void processTransferDestinationPatterns() {
        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest request =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest();

        request.setDestinationsBySouce(destinationBySource(transferDestinationPatternsByAccount));
        request.setUserId(this.request.getUser().getId());

        if (!transferDestinationPatternsByAccount.isEmpty()) {
            controllerWrapper.updateTransferDestinationPatterns(request);
        }
    }

    private Map<se.tink.backend.core.Account, List<TransferDestinationPattern>> destinationBySource(
            Map<Account, List<TransferDestinationPattern>> transferDestinationPatternsByAccount) {
        return transferDestinationPatternsByAccount.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(CoreAccountMapper.fromAggregation(e.getKey()), e.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }


    public void processEinvoices() {
        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest updateTransfersRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest();
        updateTransfersRequest.setUserId(request.getUser().getId());
        updateTransfersRequest.setCredentialsId(request.getCredentials().getId());
        updateTransfersRequest.setTransfers(transfers);

        controllerWrapper.processEinvoices(updateTransfersRequest);
    }


}
