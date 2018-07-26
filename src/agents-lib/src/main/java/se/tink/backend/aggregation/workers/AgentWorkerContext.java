package se.tink.backend.aggregation.workers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.UniformInterfaceException;
import io.dropwizard.lifecycle.Managed;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.UpdateCredentialsRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.coordination.BarrierName;
import se.tink.backend.common.mapper.CoreAccountMapper;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.repository.mysql.aggregation.AggregationCredentialsRepository;
import se.tink.backend.common.utils.MetricsUtils;
import se.tink.backend.common.utils.Pair;
import se.tink.backend.core.AggregationCredentials;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.backend.core.enums.TinkFeature;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.encryption.api.EncryptionService;
import se.tink.backend.encryption.rpc.EncryptionKeySet;
import se.tink.backend.encryption.rpc.EncryptionRequest;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.system.rpc.ProcessAccountsRequest;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.system.rpc.UpdateDocumentRequest;
import se.tink.backend.system.rpc.UpdateDocumentResponse;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;
import se.tink.backend.system.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.system.rpc.UpdateTransfersRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.libraries.uuid.UUIDUtils;

public class AgentWorkerContext extends AgentContext implements Managed, SetAccountsToAggregateContext {
    private static final AggregationLogger log = new AggregationLogger(AgentWorkerContext.class);

    private static final Set<AccountTypes> TARGET_ACCOUNT_TYPES = new HashSet<>(Arrays.asList(
            AccountTypes.CHECKING,
            AccountTypes.SAVINGS,
            AccountTypes.CREDIT_CARD,
            AccountTypes.LOAN,
            AccountTypes.INVESTMENT));

    private static final List<Integer> TRANSACTION_DATE_HISTORY_BUCKETS = ImmutableList.of(0, 30, 90, 182, 365, 730);
    private static final List<Integer> TRANSACTION_COUNT_HISTORY_BUCKETS = ImmutableList.of(0, 50, 100, 250, 500, 1000,
            2000, 5000, 10000);

    private static final String EMPTY_CLASS_NAME = "";
    private final Timer providerUpdateAccountTimer;
    private final MetricRegistry metricRegistry;
    private final Counter refreshTotal;
    private final Counter noTransferDestinationFetched;
    private final MetricId.MetricLabels defaultMetricLabels;
    private Agent agent;
    private Catalog catalog;
    private CuratorFramework coordinationClient;
    private CredentialsRequest request;
    private ServiceContext serviceContext;
    private long timeLeavingQueue;
    private long timePutOnQueue;
    private Map<String, List<Transaction>> transactionsByAccount = Maps.newHashMap();
    private Map<Account, List<TransferDestinationPattern>> transferDestinationPatternsByAccount = Maps.newHashMap();
    private List<Transfer> transfers = Lists.newArrayList();
    private AggregationCredentialsRepository aggregationCredentialsRepository;
    private List<AgentEventListener> eventListeners = Lists.newArrayList();
    private SystemServiceFactory systemServiceFactory;
    private SupplementalInformationController supplementalInformationController;
    private boolean useAggregationController;
    private AggregationControllerAggregationClient aggregationControllerAggregationClient;
    //private final ClusterInfo clusterInfo;
    private boolean isAggregationCluster;
    //Cached accounts have not been sent to system side yet.
    private Map<String, Pair<Account, AccountFeatures>> cachedAccountsByUniqueId;
    //Updated accounts have been sent to System side and has been updated with their stored Tink Id
    private Map<String, Account> updatedAccountsByTinkId;
    //White listed accounts for accounts to aggregate with
    private List<Account> whiteListedAccounts;

    public AgentWorkerContext(CredentialsRequest request, ServiceContext serviceContext, MetricRegistry metricRegistry,
            boolean useAggregationController, AggregationControllerAggregationClient aggregationControllerAggregationClient,
            ClusterInfo clusterInfo) {

        final ClusterId clusterId = clusterInfo.getClusterId();

        this.cachedAccountsByUniqueId = Maps.newHashMap();
        this.updatedAccountsByTinkId = Maps.newHashMap();
        this.whiteListedAccounts = Lists.newArrayList();

        this.request = request;
        this.serviceContext = serviceContext;

        // _Not_ instanciating a SystemService from the ServiceFactory here.
        this.systemServiceFactory = serviceContext.getSystemServiceFactory();
        this.coordinationClient = serviceContext.getCoordinationClient();

        this.aggregationCredentialsRepository = serviceContext.getRepository(AggregationCredentialsRepository.class);
        setClusterInfo(clusterInfo);
        setAggregator(clusterId.getAggregator());

        if (request.getUser() != null) {
            this.catalog = Catalog.getCatalog(request.getUser().getProfile().getLocale());
        }

        this.metricRegistry = metricRegistry;
        this.timePutOnQueue = System.currentTimeMillis();

        this.supplementalInformationController = new SupplementalInformationController(serviceContext.getCacheClient(),
                serviceContext.getCoordinationClient());
        this.useAggregationController = useAggregationController;
        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;
        this.isAggregationCluster = serviceContext.isAggregationCluster();

        Provider provider = request.getProvider();

        defaultMetricLabels = new MetricId.MetricLabels()
                .addAll(clusterId.metricLabels())
                .add("provider_type", provider.getMetricTypeName())
                .add("provider", MetricsUtils.cleanMetricName(provider.getName()))
                .add("market", provider.getMarket())
                .add("agent", Optional.ofNullable(provider.getClassName()).orElse(EMPTY_CLASS_NAME))
                .add("manual", String.valueOf(request.isManual()))
                .add("credential", request.getCredentials().getMetricTypeName())
                .add("request_type", request.getType().name());

        providerUpdateAccountTimer = metricRegistry.timer(
                MetricId.newId("update_account")
                        .label(defaultMetricLabels));

        noTransferDestinationFetched = metricRegistry.meter(
                MetricId.newId("no_transfer_destination_fetched")
                        .label(defaultMetricLabels));

        refreshTotal = metricRegistry.meter(
                MetricId.newId("accounts_refresh")
                        .label(defaultMetricLabels));
    }

    public SystemServiceFactory getSystemServiceFactory() {
        return systemServiceFactory;
    }

    public void addEventListener(AgentEventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void removeEventListener(AgentEventListener eventListener) {
        eventListeners.remove(eventListener);
    }

    @Override
    public void clear() {
        transactionsByAccount.clear();
        cachedAccountsByUniqueId.clear();
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
    public Catalog getCatalog() {
        return catalog;
    }

    @Override
    public CuratorFramework getCoordinationClient() {
        return coordinationClient;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public CredentialsRequest getRequest() {
        return request;
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
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

    @Override
    public void processAccounts() {
        Credentials credentials = request.getCredentials();

        if (credentials.getStatus() != CredentialsStatus.UPDATING) {
            log.warn(String.format("Status does not warrant account processing: %s", credentials.getStatus()));
            return;
        }

        // Metrics
        refreshTotal.inc();
        TARGET_ACCOUNT_TYPES.forEach(accountType -> {
            if (cachedAccountsByUniqueId.values().stream().noneMatch(pair-> pair.first.getType() == accountType)) {
                metricRegistry.meter(
                        MetricId.newId("no_accounts_fetched")
                                .label(defaultMetricLabels)
                                .label("type", accountType.toString())
                ).inc();
            }
        });

        // Requires Accounts in list to have been "updated" towards System's UpdateService to get their real stored id
        List<String> accountIds = Lists.newArrayList(updatedAccountsByTinkId.keySet());

        if (useAggregationController) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest processAccountsRequest =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest();
            processAccountsRequest.setAccountIds(accountIds);
            processAccountsRequest.setCredentialsId(credentials.getId());
            processAccountsRequest.setUserId(request.getUser().getId());

            if (isAggregationCluster) {
                aggregationControllerAggregationClient.processAccounts(getClusterInfo(),
                        processAccountsRequest);
            } else {
                aggregationControllerAggregationClient.processAccounts(processAccountsRequest);
            }
        } else {
            ProcessAccountsRequest processAccountsRequest = new ProcessAccountsRequest();

            processAccountsRequest.setAccountIds(accountIds);
            processAccountsRequest.setCredentialsId(credentials.getId());
            processAccountsRequest.setUserId(request.getUser().getId());

            systemServiceFactory.getUpdateService().processAccounts(processAccountsRequest);
        }
    }

    private int countDaysFromOldestTransaction(List<Transaction> transactions) {
        Optional<Date> oldestTransactionDate = transactions.stream()
                .min(Comparator.comparing(Transaction::getDate))
                .map(Transaction::getDate);

        return oldestTransactionDate.map(date -> DateUtils.daysBetween(date, new Date())).orElse(0);
    }

    private int countNumberOfTransactionsOlderThanToday(List<Transaction> transactions) {
        final Date today = new Date();
        return (int) transactions.stream()
                .filter(t -> DateUtils.daysBetween(t.getDate(), today) > 0)
                .count();
    }

    private AccountTypes getAccountTypeFor(String accountId) {
        return getAccount(accountId)
                .map(Account::getType)
                .orElse(AccountTypes.OTHER);
    }

    private Date getCertainDateFor(String accountId) {
        return getAccount(accountId)
                .map(Account::getCertainDate)
                .orElse(null);
    }

    private Optional<Account> getAccount(String accountId) {
        return Optional.ofNullable(updatedAccountsByTinkId.get(accountId));
    }

    @Override
    public void processTransactions() {
        Credentials credentials = request.getCredentials();

        List<Transaction> transactions = Lists.newArrayList();

        for (String accountId : transactionsByAccount.keySet()) {
            Optional<Account> account = request.getAccounts().stream()
                    .filter(a -> Objects.equals(a.getId(), accountId))
                    .findFirst();

            if (account.isPresent() && shouldNotAggregateDataForAccount(account.get())) {
                // Account marked to not aggregate data from.
                // Preferably we would not even download the data but this makes sure
                // we don't process further or store the account's data.
                continue;
            }

            List<Transaction> accountTransactions = transactionsByAccount.get(accountId);

            if (!credentials.isDemoCredentials()) {
                String accountType = getAccountTypeFor(accountId).name();
                boolean isFullRefresh = getCertainDateFor(accountId) == null;
                MetricId.MetricLabels labels = defaultMetricLabels.add("account_type", accountType)
                        .add("full_refresh", Boolean.toString(isFullRefresh));

                String stringifiedLabels = String.format(
                        "{\"request_type\": %s, \"full_refresh\": %s, \"provider\": %s, \"account_type\": %s}",
                        request.getType().name(), isFullRefresh, request.getProvider().getName(), accountType);

                // Metric for days between oldest & most recent transaction
                Histogram transactionDateHistory = metricRegistry
                        .histogram(MetricId.newId("transaction_history_days_total")
                                .label(labels), TRANSACTION_DATE_HISTORY_BUCKETS);
                int daysFromOldestTransaction = countDaysFromOldestTransaction(accountTransactions);
                transactionDateHistory.update(daysFromOldestTransaction);
                log.info(String.format("Days between oldest & most recent transaction: %s | %s",
                        daysFromOldestTransaction, stringifiedLabels));

                // Metric for number of transactions
                Histogram transactionCountHistory = metricRegistry
                        .histogram(MetricId.newId("transaction_history_count_total")
                                .label(labels), TRANSACTION_COUNT_HISTORY_BUCKETS);
                int numberOfTransactions = countNumberOfTransactionsOlderThanToday(accountTransactions);
                transactionCountHistory.update(numberOfTransactions);
                log.info(String.format("Number of fetched transactions: %s | %s",
                        numberOfTransactions, stringifiedLabels));
            }

            transactions.addAll(accountTransactions);
        }

        if (credentials.getStatus() != CredentialsStatus.UPDATING) {
            log.warn(String.format("Status does not warrant transaction processing: %s", credentials.getStatus()));
            return;
        }

        // If fraud credentials, update the statistics and activities.

        if (credentials.getType() == CredentialsTypes.FRAUD) {
            if (useAggregationController) {
                se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest generateStatisticsReq =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest();
                generateStatisticsReq.setUserId(request.getUser().getId());
                generateStatisticsReq.setCredentialsId(request.getCredentials().getId());
                generateStatisticsReq.setUserTriggered(request.isCreate());
                generateStatisticsReq.setMode(StatisticMode.FULL); // To trigger refresh of residences.

                if (isAggregationCluster) {
                    aggregationControllerAggregationClient.generateStatisticsAndActivityAsynchronously(
                            getClusterInfo(), generateStatisticsReq);
                } else {
                    aggregationControllerAggregationClient.generateStatisticsAndActivityAsynchronously(
                            generateStatisticsReq);
                }
            } else{
                GenerateStatisticsAndActivitiesRequest generateStatisticsReq = new GenerateStatisticsAndActivitiesRequest();
                generateStatisticsReq.setUserId(request.getUser().getId());
                generateStatisticsReq.setCredentialsId(request.getCredentials().getId());
                generateStatisticsReq.setUserTriggered(request.isCreate());
                generateStatisticsReq.setMode(StatisticMode.FULL); // To trigger refresh of residences.

                systemServiceFactory.getProcessService().generateStatisticsAndActivityAsynchronously(generateStatisticsReq);
            }
            return;
        }

        // If we don't get any transactions, just update the credentials. If
        // we do, we actually update the status of the credentials when
        // we've both processed the transactions and generated the
        // statistics.

        if (!isWaitingOnConnectorTransactions() && transactions.isEmpty()) {
            log.info("Empty transaction list, considering UPDATED to be the case ("
                    + request.getCredentials().getId() + ")");

            Date now = new Date();

            credentials.setUpdated(now);
            credentials.setStatus(CredentialsStatus.UPDATED);

            updateCredentialsExcludingSensitiveInformation(credentials);

            return;
        }

        // Send the request to process the transactions that we've collected in this batch.

        if (useAggregationController) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest updateTransactionsRequest =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest();
            updateTransactionsRequest.setTransactions(transactions);
            updateTransactionsRequest.setUser(credentials.getUserId());
            updateTransactionsRequest.setCredentials(credentials.getId());
            updateTransactionsRequest.setUserTriggered(request.isManual());

            if (isAggregationCluster) {
                aggregationControllerAggregationClient.updateTransactionsAsynchronously(getClusterInfo(),
                        updateTransactionsRequest);
            } else {
                aggregationControllerAggregationClient.updateTransactionsAsynchronously(updateTransactionsRequest);
            }

        } else {
            UpdateTransactionsRequest updateTransactionsRequest = new UpdateTransactionsRequest();
            updateTransactionsRequest.setTransactions(transactions);
            updateTransactionsRequest.setUser(credentials.getUserId());
            updateTransactionsRequest.setCredentials(credentials.getId());
            updateTransactionsRequest.setUserTriggered(request.isManual());

            systemServiceFactory.getProcessService().updateTransactionsAsynchronously(updateTransactionsRequest);
        }

        // Don't use the queue yet
        //
        //        UpdateTransactionsTask task = new UpdateTransactionsTask();
        //        task.setPayload(updateTransactionsRequest);
        //
        //        taskSubmitter.submit(task);
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        String supplementalInformation = null;

        if (wait) {
            DistributedBarrier lock = new DistributedBarrier(coordinationClient,
                    BarrierName.build(BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, request.getCredentials().getId()));

            try {
                // Reset barrier.

                lock.removeBarrier();
                lock.setBarrier();

                updateCredentialsExcludingSensitiveInformation(credentials);

                // Wait for the barrier and the supplemental information.

                if (lock.waitOnBarrier(2, TimeUnit.MINUTES)) {

                    supplementalInformation = supplementalInformationController.getSupplementalInformation(
                                credentials.getId());

                    credentials.setSupplementalInformation(supplementalInformation);
                    // TODO: We've noticed that we get app crashes for Sparbanken Syd and we believe the reason is that
                    //       we trigger an new bank id without supplemental information as null.
                    //       Let's remove comment this out. If we haven't noticed any problems remove the code
                    //       and this comment.
                    // updateCredentialsExcludingSensitiveInformation(credentials);

                    if (Objects.equals(supplementalInformation, "null")) {
                        log.info("Supplemental information request was cancelled by client (returned null)");
                        supplementalInformation = null;
                    }
                } else {
                    log.info("Supplemental information request timed out");
                    // Did not get lock, release anyways and return.
                    lock.removeBarrier();
                }
            } catch (Exception e) {
                log.error("Caught exception while waiting for supplemental information", e);
            }
        } else {
            updateStatus(credentials.getStatus());
        }

        return supplementalInformation;
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        Credentials credentials = request.getCredentials();

        credentials.setSupplementalInformation(autoStartToken);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        requestSupplementalInformation(credentials, wait);
    }

    private boolean shouldNotAggregateDataForAccount(Account account) {
        Optional<Account> existingAccount = request.getAccounts().stream()
                .filter(a -> Objects.equals(a.getBankId(), account.getBankId()))
                .findFirst();

        return existingAccount.isPresent() &&
                existingAccount.get().getAccountExclusion().excludedFeatures.contains(TinkFeature.AGGREGATION);
    }

    @Override
    public void cacheAccount(Account account, AccountFeatures accountFeatures) {

        if (shouldNotAggregateDataForAccount(account)) {
            // Account marked to not aggregate data from.
            // Preferably we would not even download the data but this makes sure
            // we don't process further or store the account's data.
            return;
        }

        cachedAccountsByUniqueId.put(account.getBankId(), new Pair<>(account, accountFeatures));
    }

    public void sendAllCachedAccountsToUpdateService() {
        for(String uniqueId : cachedAccountsByUniqueId.keySet()) {
            sendAccountToUpdateService(uniqueId);
        }
    }

    public Account sendAccountToUpdateService(String uniqueId) {

        Pair<Account, AccountFeatures> pair = cachedAccountsByUniqueId.get(uniqueId);

        Account account = pair.first;
        AccountFeatures accountFeatures = pair.second;

        account.setCredentialsId(request.getCredentials().getId());
        account.setUserId(request.getCredentials().getUserId());

        Account updatedAccount;
        if (useAggregationController) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest updateAccountRequest =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest();

            updateAccountRequest.setUser(request.getCredentials().getUserId());
            // TODO: Refactor System API side to not depend on :main-api
            updateAccountRequest.setAccount(CoreAccountMapper.fromAggregation(account));
            updateAccountRequest.setAccountFeatures(accountFeatures);
            updateAccountRequest.setCredentialsId(request.getCredentials().getId());

            Context updateAccountTimerContext = providerUpdateAccountTimer.time();

            try {
                if (isAggregationCluster) {
                    updatedAccount = aggregationControllerAggregationClient.updateAccount(getClusterInfo(),
                            updateAccountRequest);
                } else {
                    updatedAccount = aggregationControllerAggregationClient.updateAccount(updateAccountRequest);
                }

            } catch (UniformInterfaceException e) {
                log.error("Account update request failed, response: " +
                        (e.getResponse().hasEntity() ? e.getResponse().getEntity(String.class) : ""));
                throw e;
            }

            updateAccountTimerContext.stop();
        } else {
            UpdateAccountRequest updateAccountsRequest = new UpdateAccountRequest();

            updateAccountsRequest.setUser(request.getCredentials().getUserId());
            // TODO: Refactor System API side to not depend on :main-api
            updateAccountsRequest.setAccount(CoreAccountMapper.fromAggregation(account));
            updateAccountsRequest.setAccountFeatures(accountFeatures);
            updateAccountsRequest.setCredentialsId(request.getCredentials().getId());

            Context updateAccountTimerContext = providerUpdateAccountTimer.time();

            try {
                updatedAccount = CoreAccountMapper.toAggregation(
                        systemServiceFactory.getUpdateService().updateAccount(updateAccountsRequest));
            } catch (UniformInterfaceException e) {
                log.error("Account update request failed, response: " +
                        (e.getResponse().hasEntity() ? e.getResponse().getEntity(String.class) : ""));
                throw e;
            }

            updateAccountTimerContext.stop();
        }

        updatedAccountsByTinkId.put(updatedAccount.getId(), updatedAccount);

        return updatedAccount;
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(Credentials credentials) {
        // Execute any event-listeners.

        for (AgentEventListener eventListener : eventListeners) {
            eventListener.onUpdateCredentialsStatus();
        }

        // Clone the credentials here so that we can pass a copy with no
        // secrets back to the system service.

        Credentials credentialsCopy = credentials.clone();
        credentialsCopy.clearSensitiveInformation(request.getProvider());

        // TODO: Refactor System API side to not depend on :main-api
        se.tink.backend.core.Credentials coreCredentials = CoreCredentialsMapper
                .fromAggregationCredentials(credentialsCopy);

        if (useAggregationController) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest updateCredentialsStatusRequest =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest();
            updateCredentialsStatusRequest.setCredentials(coreCredentials);
            updateCredentialsStatusRequest.setUserId(credentials.getUserId());
            updateCredentialsStatusRequest.setUpdateContextTimestamp(
                    request instanceof UpdateCredentialsRequest ||
                            request instanceof CreateCredentialsRequest);
            updateCredentialsStatusRequest.setManual(request.isManual());
            updateCredentialsStatusRequest.setUserDeviceId(request.getUserDeviceId());

            if (isAggregationCluster) {
                aggregationControllerAggregationClient.updateCredentials(getClusterInfo(),
                        updateCredentialsStatusRequest);
            } else {
                aggregationControllerAggregationClient.updateCredentials(updateCredentialsStatusRequest);
            }

        } else {
            UpdateCredentialsStatusRequest updateCredentialsStatusRequest = new UpdateCredentialsStatusRequest();
            updateCredentialsStatusRequest.setCredentials(coreCredentials);
            updateCredentialsStatusRequest.setUserId(credentials.getUserId());
            updateCredentialsStatusRequest.setUpdateContextTimestamp(
                    request instanceof UpdateCredentialsRequest ||
                            request instanceof CreateCredentialsRequest);
            updateCredentialsStatusRequest.setManual(request.isManual());
            updateCredentialsStatusRequest.setUserDeviceId(request.getUserDeviceId());

            systemServiceFactory.getUpdateService().updateCredentials(updateCredentialsStatusRequest);
        }
    }

    @Override
    public void updateFraudDetailsContent(List<FraudDetailsContent> detailsContents) {
        UpdateFraudDetailsRequest updateFraudRequest = new UpdateFraudDetailsRequest();
        updateFraudRequest.setUserId(request.getUser().getId());
        updateFraudRequest.setDetailsContents(detailsContents);

        systemServiceFactory.getUpdateService().updateFraudDetails(updateFraudRequest);
    }

    @Override
    public void updateStatus(CredentialsStatus status) {
        updateStatus(status, null);
    }

    @Override
    public void updateStatus(final CredentialsStatus status, final String statusPayload,
            final boolean statusFromProvider) {

        Credentials credentials = request.getCredentials();
        credentials.setStatus(status);
        credentials.setStatusPayload(statusPayload);

        if (statusFromProvider
                && statusPayload != null
                && (status == CredentialsStatus.AUTHENTICATION_ERROR
                || status == CredentialsStatus.TEMPORARY_ERROR)) {

            String payload;

            if (Objects.equals(serviceContext.getConfiguration().getCluster(), Cluster.ABNAMRO)) {
                payload = statusPayload;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(
                        catalog.getString("Error from") + " " + request.getProvider().getDisplayName() + ":");
                buffer.append(" \"");
                buffer.append(statusPayload);
                buffer.append("\"");

                payload = buffer.toString();
            }

            credentials.setStatusPayload(payload);
        }

        updateCredentialsExcludingSensitiveInformation(credentials);
    }

    @Override
    public Account updateTransactions(final Account account, List<Transaction> transactions) {

        if (shouldNotAggregateDataForAccount(account)) {
            // Account marked to not aggregate data from.
            // Preferably we would not even download the data but this makes sure
            // we don't process further or store the account's data.
            return account;
        }

        cacheAccount(account);
        final Account updatedAccount = sendAccountToUpdateService(account.getBankId());

        for (Transaction transaction : transactions) {
            transaction.setAccountId(updatedAccount.getId());
            transaction.setCredentialsId(request.getCredentials().getId());
            transaction.setUserId(request.getCredentials().getUserId());

            if (!Strings.isNullOrEmpty(transaction.getDescription())) {
                transaction.setDescription(transaction.getDescription().replace("<", "").replace(">", ""));
            }

            if (transaction.getType() == null) {
                transaction.setType(TransactionTypes.DEFAULT);
            }
        }

        transactionsByAccount.put(updatedAccount.getId(), transactions);

        return updatedAccount;
    }

    @Override
    public void updateTransferDestinationPatterns(
            Map<Account, List<TransferDestinationPattern>> transferDestinationPatterns) {

        for (Account account : transferDestinationPatterns.keySet()) {
            if (transferDestinationPatternsByAccount.containsKey(account)) {
                transferDestinationPatternsByAccount.get(account).addAll(transferDestinationPatterns.get(account));
            } else {
                transferDestinationPatternsByAccount.put(account, transferDestinationPatterns.get(account));
            }
        }
    }

    @Override
    public void processTransferDestinationPatterns() {
        if (useAggregationController) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest request =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest();

            request.setDestinationsBySouce(destinationBySource(transferDestinationPatternsByAccount));
            request.setUserId(this.request.getUser().getId());

            if (!transferDestinationPatternsByAccount.isEmpty()) {
                if (isAggregationCluster) {
                    aggregationControllerAggregationClient.updateTransferDestinationPatterns(getClusterInfo(),
                            request);
                } else {
                    aggregationControllerAggregationClient.updateTransferDestinationPatterns(request);
                }
            } else {
                noTransferDestinationFetched.inc();
            }
        } else {
            UpdateTransferDestinationPatternsRequest request = new UpdateTransferDestinationPatternsRequest();
            request.setDestinationsBySouce(destinationBySource(transferDestinationPatternsByAccount));
            request.setUserId(this.request.getUser().getId());

            if (!transferDestinationPatternsByAccount.isEmpty()) {
                systemServiceFactory.getUpdateService().updateTransferDestinationPatterns(request);
            } else {
                noTransferDestinationFetched.inc();
            }
        }
    }

    private Map<se.tink.backend.core.Account, List<TransferDestinationPattern>> destinationBySource(
            Map<Account, List<TransferDestinationPattern>> transferDestinationPatternsByAccount) {
        return transferDestinationPatternsByAccount.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(CoreAccountMapper.fromAggregation(e.getKey()), e.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Override
    public void updateCredentialsOnlySensitiveInformation(Credentials credentials) {
        AggregationCredentials aggregationCredentials = aggregationCredentialsRepository.findOne(credentials
                .getId());

        if (aggregationCredentials == null) {
            return;
        }

        // Prepare the encryption request.

        EncryptionRequest encryptionRequest = new EncryptionRequest();
        encryptionRequest.setKeySet(new EncryptionKeySet(credentials.getSecretKey(), aggregationCredentials
                .getSecretKey()));

        // Encrypt sensitive information.

        EncryptionService encryptionService = serviceContext.getEncryptionServiceFactory().getEncryptionService();

        Credentials sensitiveInformationCredentials = credentials.clone();
        sensitiveInformationCredentials.onlySensitiveInformation(request.getProvider());

        encryptionRequest.setPayload(sensitiveInformationCredentials.getFieldsSerialized());
        aggregationCredentials.setEncryptedFields(encryptionService.encrypt(encryptionRequest).getPayload());

        String encryptedPayload = null;

        if (!Strings.isNullOrEmpty(sensitiveInformationCredentials.getSensitivePayloadSerialized())) {
            encryptionRequest.setPayload(sensitiveInformationCredentials.getSensitivePayloadSerialized());
            encryptedPayload = encryptionService.encrypt(encryptionRequest).getPayload();
        }

        aggregationCredentials.setEncryptedPayload(encryptedPayload);

        // Save the encrypted aggregation credentials locally.

        aggregationCredentialsRepository.save(aggregationCredentials);
    }

    @Override
    public void updateSignableOperation(SignableOperation signableOperation) {
        if (useAggregationController) {
            if (isAggregationCluster) {
                aggregationControllerAggregationClient.updateSignableOperation(getClusterInfo(),
                        signableOperation);
            } else {
                aggregationControllerAggregationClient.updateSignableOperation(signableOperation);
            }
        } else{
            systemServiceFactory.getUpdateService().updateSignableOperation(signableOperation);
        }
    }

    @Override
    public UpdateDocumentResponse updateDocument(DocumentContainer container) {
        if (useAggregationController) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentRequest updateDocumentRequest =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentRequest();
            updateDocumentRequest.setUserId(request.getUser().getId());
            updateDocumentRequest.setDocumentContainer(container);

            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentResponse updateDocumentResponse;
            if (isAggregationCluster) {
                updateDocumentResponse =
                        aggregationControllerAggregationClient.updateDocument(getClusterInfo(),
                                updateDocumentRequest);
            } else {
                updateDocumentResponse = aggregationControllerAggregationClient.updateDocument(updateDocumentRequest);
            }

            if (updateDocumentResponse.isSuccessfullyStored()) {
                return UpdateDocumentResponse.createSuccessful(
                        updateDocumentResponse.getDocumentIdentifier(),
                        UUIDUtils.fromString(updateDocumentResponse.getToken()),
                        updateDocumentResponse.getFullUrl());
            } else {
                return UpdateDocumentResponse.createUnSuccessful();
            }
        } else {
            UpdateDocumentRequest updateRequest = new UpdateDocumentRequest();
            updateRequest.setUserId(request.getUser().getId());
            updateRequest.setDocumentContainer(container);

            return systemServiceFactory.getUpdateService().updateDocument(updateRequest);
        }
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public List<Account> getUpdatedAccounts() {
        return Lists.newArrayList(updatedAccountsByTinkId.values());
    }

    @Override
    public void updateEinvoices(List<Transfer> transfers) {
        if (transfers.isEmpty()) {
            return;
        }

        for (Transfer transfer : transfers) {

            // Validation

            AccountIdentifier destination = transfer.getDestination();
            if (destination == null) {
                log.warn(String.format("Ignoring transfer because it has missing destination: %s", transfer));
                continue;
            }

            if (!destination.isValid()) {
                log.warn(String.format("Ignoring non-valid transfer with identifier '%s'. Transfer: %s",
                        destination.getIdentifier(), transfer));
                continue;
            }

            // Validation passed.

            this.transfers.add(transfer);
        }
    }

    @Override
    public void processEinvoices() {
        if (useAggregationController) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest updateTransfersRequest =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest();
            updateTransfersRequest.setUserId(request.getUser().getId());
            updateTransfersRequest.setCredentialsId(request.getCredentials().getId());
            updateTransfersRequest.setTransfers(transfers);

            if (isAggregationCluster) {
                aggregationControllerAggregationClient.processEinvoices(getClusterInfo(),
                        updateTransfersRequest);
            } else {
                aggregationControllerAggregationClient.processEinvoices(updateTransfersRequest);
            }
        } else {
            UpdateTransfersRequest updateTransfersRequest = new UpdateTransfersRequest();
            updateTransfersRequest.setUserId(request.getUser().getId());
            updateTransfersRequest.setCredentialsId(request.getCredentials().getId());
            updateTransfersRequest.setTransfers(transfers);

            systemServiceFactory.getUpdateService().processEinvoices(updateTransfersRequest);
        }
    }

    @Override
    public boolean isCredentialDeleted(String credentialsId) {
        Preconditions.checkState(!Strings.isNullOrEmpty(credentialsId), "CredentialsId must not be null or empty.");
        return aggregationCredentialsRepository.findOne(credentialsId) == null;
    }

    @Override
    public void setAccounts(List<Account> accounts) {
        whiteListedAccounts = accounts;
    }
}
