package se.tink.backend.aggregation.workers;

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
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.agents.SetAccountsToAggregateContext;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.api.CallbackHostConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.common.mapper.CoreAccountMapper;
import se.tink.backend.common.utils.MetricsUtils;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class AgentWorkerCommandContext extends AgentWorkerContext implements SetAccountsToAggregateContext {
    private static final AggregationLogger log = new AggregationLogger(AgentWorkerCommandContext.class);
    protected CuratorFramework coordinationClient;
    private static final String EMPTY_CLASS_NAME = "";

    protected Agent agent;

    protected final Counter refreshTotal;
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
            CallbackHostConfiguration callbackHostConfiguration,
            SupplementalInformationController supplementalInformationController,
            ControllerWrapper controllerWrapper) {
        super(request, metricRegistry, coordinationClient, aggregatorInfo,
                callbackHostConfiguration, supplementalInformationController, controllerWrapper);
        this.coordinationClient = coordinationClient;
        this.timePutOnQueue = System.currentTimeMillis();
        this.uniqueIdOfUserSelectedAccounts = Lists.newArrayList();

        Provider provider = request.getProvider();

        defaultMetricLabels = new MetricId.MetricLabels()
                .addAll(controllerWrapper.getHostConfiguration().metricLabels())
                .add("provider", MetricsUtils.cleanMetricName(provider.getName()))
                .add("market", provider.getMarket())
                .add("agent", Optional.ofNullable(provider.getClassName()).orElse(EMPTY_CLASS_NAME))
                .add("request_type", request.getType().name());

        refreshTotal = metricRegistry.meter(
                MetricId.newId("accounts_refresh")
                        .label(defaultMetricLabels));

        this.agentsServiceConfiguration = agentsServiceConfiguration;
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
                metricRegistry.meter(
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
        for (String uniqueId : allAvailableAccountsByUniqueId.keySet()) {
            sendAccountToUpdateService(uniqueId);
        }
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
