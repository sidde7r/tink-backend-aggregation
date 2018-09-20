package se.tink.backend.aggregation.workers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.UniformInterfaceException;
import io.dropwizard.lifecycle.Managed;
import java.util.AbstractMap;
import java.util.Arrays;
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
import se.tink.backend.aggregation.agents.SetAccountsToAggregateContext;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.coordination.BarrierName;
import se.tink.backend.common.mapper.CoreAccountMapper;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.repository.mysql.aggregation.aggregationcredentials.AggregationCredentialsRepository;
import se.tink.backend.common.utils.MetricsUtils;
import se.tink.backend.common.utils.Pair;
import se.tink.backend.core.AggregationCredentials;
import se.tink.backend.core.DocumentContainer;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.account.TransferDestinationPattern;
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
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

public class AgentWorkerContext extends AgentContext implements Managed, SetAccountsToAggregateContext {
    private static final AggregationLogger log = new AggregationLogger(AgentWorkerContext.class);

    private static final Set<AccountTypes> TARGET_ACCOUNT_TYPES = new HashSet<>(Arrays.asList(
            AccountTypes.CHECKING,
            AccountTypes.SAVINGS,
            AccountTypes.CREDIT_CARD,
            AccountTypes.LOAN,
            AccountTypes.INVESTMENT));

    private static final String EMPTY_CLASS_NAME = "";
    private final MetricRegistry metricRegistry;
    private final Counter refreshTotal;
    private final MetricId.MetricLabels defaultMetricLabels;
    private Agent agent;
    private Catalog catalog;
    private CuratorFramework coordinationClient;
    private CredentialsRequest request;
    private ServiceContext serviceContext;
    private long timeLeavingQueue;
    private long timePutOnQueue;
    private Map<String, List<Transaction>> transactionsByAccountBankId = Maps.newHashMap();
    private Map<Account, List<TransferDestinationPattern>> transferDestinationPatternsByAccount = Maps.newHashMap();
    private List<Transfer> transfers = Lists.newArrayList();
    private AggregationCredentialsRepository aggregationCredentialsRepository;
    private List<AgentEventListener> eventListeners = Lists.newArrayList();
    private SystemServiceFactory systemServiceFactory;
    private SupplementalInformationController supplementalInformationController;
    private AggregationControllerAggregationClient aggregationControllerAggregationClient;
    //private final ClusterInfo clusterInfo;
    //Cached accounts have not been sent to system side yet.
    private Map<String, Pair<Account, AccountFeatures>> allAvailableAccountsByUniqueId;
    //Updated accounts have been sent to System side and has been updated with their stored Tink Id
    private Map<String, Account> updatedAccountsByTinkId;
    // a collection of account to keep a record of what accounts we should aggregate data after opt-in flow,
    // selecting white listed accounts and eliminating blacklisted accounts
    private List<Account> accountsToAggregate;
    // a collection of account numbers that the Opt-in user selected during the opt-in flow
    private List<String> uniqueIdOfUserSelectedAccounts;
    // True or false if system has been requested to process transactions.
    private boolean isSystemProcessingTransactions;
    private boolean isWhitelistRefresh;

    public AgentWorkerContext(CredentialsRequest request, ServiceContext serviceContext, MetricRegistry metricRegistry,
            AggregationControllerAggregationClient aggregationControllerAggregationClient,
            ClusterInfo clusterInfo) {

        final ClusterId clusterId = clusterInfo.getClusterId();

        this.allAvailableAccountsByUniqueId = Maps.newHashMap();
        this.updatedAccountsByTinkId = Maps.newHashMap();
        this.accountsToAggregate = Lists.newArrayList();
        this.uniqueIdOfUserSelectedAccounts = Lists.newArrayList();

        this.request = request;
        this.serviceContext = serviceContext;

        // _Not_ instanciating a SystemService from the ServiceFactory here.
        this.systemServiceFactory = serviceContext.getSystemServiceFactory();
        this.coordinationClient = serviceContext.getCoordinationClient();

        this.aggregationCredentialsRepository = serviceContext.getRepository(AggregationCredentialsRepository.class);
        setClusterInfo(clusterInfo);
        setAggregator(clusterInfo.getAggregator());

        if (request.getUser() != null) {
            this.catalog = Catalog.getCatalog(request.getUser().getProfile().getLocale());
        }

        this.metricRegistry = metricRegistry;
        this.timePutOnQueue = System.currentTimeMillis();

        this.supplementalInformationController = new SupplementalInformationController(serviceContext.getCacheClient(),
                serviceContext.getCoordinationClient());
        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;

        Provider provider = request.getProvider();

        defaultMetricLabels = new MetricId.MetricLabels()
                .addAll(clusterId.metricLabels())
                .add("provider", MetricsUtils.cleanMetricName(provider.getName()))
                .add("market", provider.getMarket())
                .add("agent", Optional.ofNullable(provider.getClassName()).orElse(EMPTY_CLASS_NAME))
                .add("request_type", request.getType().name());

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

    public boolean isSystemProcessingTransactions() {
        return isSystemProcessingTransactions;
    }

    @Override
    public void clear() {
        transactionsByAccountBankId.clear();
        allAvailableAccountsByUniqueId.clear();
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

        aggregationControllerAggregationClient.processAccounts(getClusterInfo(),
                processAccountsRequest);
    }

    @Override
    public void processTransactions() {
        Credentials credentials = request.getCredentials();

        List<Transaction> transactions = Lists.newArrayList();

        for (String bankId : transactionsByAccountBankId.keySet()) {
            Optional<Account> account = getUpdatedAccounts().stream()
                    .filter(a -> Objects.equals(a.getBankId(), bankId))
                    .findFirst();

            if (!account.isPresent()) {
                if (!isWhitelistRefresh) {
                    log.error("Account not found in updated Accounts list. "
                            + "This should not happen and might mean that Agent is not updating all Accounts separately.");
                }

                continue;
            }

            if (!shouldAggregateDataForAccount(account.get())) {
                // Account marked to not aggregate data from.
                // Preferably we would not even download the data but this makes sure
                // we don't process further or store the account's data.
                continue;
            }

            List<Transaction> accountTransactions = transactionsByAccountBankId.get(bankId);

            for (Transaction transaction : accountTransactions) {
                transaction.setAccountId(account.get().getId());
                transaction.setCredentialsId(request.getCredentials().getId());
                transaction.setUserId(request.getCredentials().getUserId());

                if (!Strings.isNullOrEmpty(transaction.getDescription())) {
                    transaction.setDescription(transaction.getDescription().replace("<", "").replace(">", ""));
                }

                if (transaction.getType() == null) {
                    transaction.setType(TransactionTypes.DEFAULT);
                }
            }

            transactions.addAll(accountTransactions);
        }

        if (credentials.getStatus() != CredentialsStatus.UPDATING) {
            log.warn(String.format("Status does not warrant transaction processing: %s", credentials.getStatus()));
            return;
        }

        // If fraud credentials, update the statistics and activities.

        if (credentials.getType() == CredentialsTypes.FRAUD) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest generateStatisticsReq =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest();
            generateStatisticsReq.setUserId(request.getUser().getId());
            generateStatisticsReq.setCredentialsId(request.getCredentials().getId());
            generateStatisticsReq.setUserTriggered(request.isCreate());
            generateStatisticsReq.setMode(StatisticMode.FULL); // To trigger refresh of residences.

            aggregationControllerAggregationClient.generateStatisticsAndActivityAsynchronously(
                    getClusterInfo(), generateStatisticsReq);
            return;
        }

        // Don't update transactions if we didn't get any
        if (transactions.isEmpty()) {
            return;
        }

        // Send the request to process the transactions that we've collected in this batch.

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest updateTransactionsRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest();
        updateTransactionsRequest.setTransactions(transactions);
        updateTransactionsRequest.setUser(credentials.getUserId());
        updateTransactionsRequest.setCredentials(credentials.getId());
        updateTransactionsRequest.setUserTriggered(request.isManual());

        aggregationControllerAggregationClient.updateTransactionsAsynchronously(getClusterInfo(),
                updateTransactionsRequest);

        isSystemProcessingTransactions = true;

        // Don't use the queue yet
        //
        //        UpdateTransactionsTask task = new UpdateTransactionsTask();
        //        task.setPayload(updateTransactionsRequest);
        //
        //        taskSubmitter.submit(task);
    }

    @Override
    public Optional<String> waitForSupplementalInformation(String key, long waitFor, TimeUnit unit) {
        DistributedBarrier lock = new DistributedBarrier(coordinationClient,
                BarrierName.build(BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, key));
        try {
            // Reset barrier.
            lock.removeBarrier();
            lock.setBarrier();

            if (lock.waitOnBarrier(waitFor, unit)) {
                String supplementalInformation = supplementalInformationController.getSupplementalInformation(key);

                if (Objects.equals(supplementalInformation, "null")) {
                    log.info("Supplemental information request was cancelled by client (returned null)");
                    return Optional.empty();
                }

                return Optional.ofNullable(supplementalInformation);
            } else {
                log.info("Supplemental information request timed out");
                // Did not get lock, release anyways and return.
                lock.removeBarrier();
            }

        } catch (Exception e) {
            log.error("Caught exception while waiting for supplemental information", e);
        } finally {
            // Always clean up the supplemental information
            Credentials credentials = request.getCredentials();
            credentials.setSupplementalInformation(null);
        }

        return Optional.empty();
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        if (wait) {
            updateCredentialsExcludingSensitiveInformation(credentials, true);

            Optional<String> supplementalInformation = waitForSupplementalInformation(credentials.getId(), 2,
                    TimeUnit.MINUTES);

            return supplementalInformation.orElse(null);
        } else {
            updateStatus(credentials.getStatus());
        }

        return null;
    }

    @Override
    public void openBankId(String autoStartToken, boolean wait) {
        Credentials credentials = request.getCredentials();

        credentials.setSupplementalInformation(autoStartToken);
        credentials.setStatus(CredentialsStatus.AWAITING_MOBILE_BANKID_AUTHENTICATION);

        requestSupplementalInformation(credentials, wait);
    }

    private boolean shouldAggregateDataForAccount(Account account) {
        return accountsToAggregate.stream().map(Account::getBankId).collect(Collectors.toList())
                .contains(account.getBankId());
    }

    @Override
    public void cacheAccount(Account account, AccountFeatures accountFeatures) {
        AccountFeatures accountFeaturesToCache = accountFeatures;

        if (allAvailableAccountsByUniqueId.containsKey(account.getBankId())) {
            // FIXME This whole if-case is a result of having Agents calling cacheAccounts multiple times. Sometimes
            // FIXME with accountFeatures and sometimes without.
            Pair<Account, AccountFeatures> pair = allAvailableAccountsByUniqueId.get(account.getBankId());
            if (accountFeatures.isEmpty() && !pair.second.isEmpty()) {
                accountFeaturesToCache = pair.second;
            }
        }

        allAvailableAccountsByUniqueId.put(account.getBankId(), new Pair<>(account, accountFeaturesToCache));
    }

    @Override
    public Optional<AccountFeatures> getAccountFeatures(final String uniqueAccountId) {
        return Optional.ofNullable(allAvailableAccountsByUniqueId.get(uniqueAccountId)).map(p -> p.second);
    }

    public void sendAllCachedAccountsToUpdateService() {
        for (String uniqueId : allAvailableAccountsByUniqueId.keySet()) {
            sendAccountToUpdateService(uniqueId);
        }
    }

    public Account sendAccountToUpdateService(String uniqueId) {

        Pair<Account, AccountFeatures> pair = allAvailableAccountsByUniqueId.get(uniqueId);

        Account account = pair.first;
        AccountFeatures accountFeatures = pair.second;

        if (!shouldAggregateDataForAccount(account)) {
            // Account marked to not aggregate data from.
            // Preferably we would not even download the data but this makes sure
            // we don't process further or store the account's data.
            return account;
        }

        account.setCredentialsId(request.getCredentials().getId());
        account.setUserId(request.getCredentials().getUserId());

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest updateAccountRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest();

        updateAccountRequest.setUser(request.getCredentials().getUserId());
        // TODO: Refactor System API side to not depend on :main-api
        updateAccountRequest.setAccount(CoreAccountMapper.fromAggregation(account));
        updateAccountRequest.setAccountFeatures(accountFeatures);
        updateAccountRequest.setCredentialsId(request.getCredentials().getId());

        Account updatedAccount;
        try {
            updatedAccount = aggregationControllerAggregationClient.updateAccount(getClusterInfo(),
                    updateAccountRequest);

        } catch (UniformInterfaceException e) {
            log.error("Account update request failed, response: " +
                    (e.getResponse().hasEntity() ? e.getResponse().getEntity(String.class) : ""));
            throw e;
        }

        updatedAccountsByTinkId.put(updatedAccount.getId(), updatedAccount);

        return updatedAccount;
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(Credentials credentials, boolean doStatusUpdate) {
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

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest updateCredentialsStatusRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest();
        updateCredentialsStatusRequest.setCredentials(coreCredentials);
        updateCredentialsStatusRequest.setUserId(credentials.getUserId());
        updateCredentialsStatusRequest.setUpdateContextTimestamp(doStatusUpdate);
        updateCredentialsStatusRequest.setUserDeviceId(request.getUserDeviceId());

        aggregationControllerAggregationClient.updateCredentials(getClusterInfo(),
                updateCredentialsStatusRequest);
    }

    @Override
    public void updateFraudDetailsContent(List<FraudDetailsContent> detailsContents) {
        UpdateFraudDetailsRequest updateFraudRequest = new UpdateFraudDetailsRequest();
        updateFraudRequest.setUserId(request.getUser().getId());
        updateFraudRequest.setDetailsContents(detailsContents);

        aggregationControllerAggregationClient.updateFraudDetails(getClusterInfo(),
                updateFraudRequest);
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

        updateCredentialsExcludingSensitiveInformation(credentials, true);
    }

    @Override
    @Deprecated // Use cacheTransactions instead
    public Account updateTransactions(final Account account, List<Transaction> transactions) {

        cacheAccount(account);
        transactionsByAccountBankId.put(account.getBankId(), transactions);

        return account;
    }

    @Override
    public void cacheTransactions(String accountUniqueId, List<Transaction> transactions) {
        // This crashes if agent is implemented incorrectly. You have to cache Account before you cache Transactions
        Preconditions.checkArgument(allAvailableAccountsByUniqueId.containsKey(accountUniqueId));
        transactionsByAccountBankId.put(accountUniqueId, transactions);
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
        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest request =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest();

        request.setDestinationsBySouce(destinationBySource(transferDestinationPatternsByAccount));
        request.setUserId(this.request.getUser().getId());

        if (!transferDestinationPatternsByAccount.isEmpty()) {
            aggregationControllerAggregationClient.updateTransferDestinationPatterns(getClusterInfo(),
                    request);
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
        aggregationControllerAggregationClient.updateSignableOperation(getClusterInfo(),
                signableOperation);
    }

    @Override
    public UpdateDocumentResponse updateDocument(DocumentContainer container) {
        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentRequest updateDocumentRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentRequest();
        updateDocumentRequest.setUserId(request.getUser().getId());
        updateDocumentRequest.setDocumentContainer(container);

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentResponse updateDocumentResponse;
        updateDocumentResponse = aggregationControllerAggregationClient.updateDocument(getClusterInfo(),
                updateDocumentRequest);

        if (updateDocumentResponse.isSuccessfullyStored()) {
            return UpdateDocumentResponse.createSuccessful(
                    updateDocumentResponse.getDocumentIdentifier(),
                    UUIDUtils.fromString(updateDocumentResponse.getToken()),
                    updateDocumentResponse.getFullUrl());
        } else {
            return UpdateDocumentResponse.createUnSuccessful();
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
        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest updateTransfersRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest();
        updateTransfersRequest.setUserId(request.getUser().getId());
        updateTransfersRequest.setCredentialsId(request.getCredentials().getId());
        updateTransfersRequest.setTransfers(transfers);

        aggregationControllerAggregationClient.processEinvoices(getClusterInfo(),
                updateTransfersRequest);
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
}
