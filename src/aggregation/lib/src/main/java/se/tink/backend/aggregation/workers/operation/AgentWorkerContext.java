package se.tink.backend.aggregation.workers.operation;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.UniformInterfaceException;
import io.dropwizard.lifecycle.Managed;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.models.fraud.FraudDetailsContent;
import se.tink.backend.aggregation.agents.utils.mappers.CoreAccountMapper;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountHolderRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.Psd2PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.backend.aggregation.compliance.regulatory_restrictions.RegulatoryRestrictions;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.locks.BarrierName;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.enums.StatisticMode;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.transfer.rpc.Transfer;

public class AgentWorkerContext extends AgentContext implements Managed {
    private static final AggregationLogger log = new AggregationLogger(AgentWorkerContext.class);

    private Catalog catalog;
    protected CuratorFramework coordinationClient;
    protected CredentialsRequest request;
    private final AccountDataCache accountDataCache;

    private Map<String, List<Transaction>> transactionsByAccountBankId = Maps.newHashMap();
    protected Map<Account, List<TransferDestinationPattern>> transferDestinationPatternsByAccount =
            Maps.newHashMap();
    protected List<Transfer> transfers = Lists.newArrayList();
    protected List<AgentEventListener> eventListeners = Lists.newArrayList();
    private SupplementalInformationController supplementalInformationController;
    private ProviderSessionCacheController providerSessionCacheController;
    private final Psd2PaymentAccountClassifier psd2PaymentAccountClassifier;

    private static class SupplementalInformationMetrics {
        private static final String CLUSTER_LABEL = "client_cluster";
        public static final MetricId duration =
                MetricId.newId("aggregation_supplemental_information_seconds");
        public static final MetricId attempts =
                MetricId.newId("aggregation_supplemental_information_requests_started");
        public static final MetricId finished =
                MetricId.newId("aggregation_supplemental_information_requests_finished");
        public static final MetricId cancelled =
                MetricId.newId("aggregation_supplemental_information_requests_cancelled");
        public static final MetricId timedOut =
                MetricId.newId("aggregation_supplemental_information_requests_timed_out");
        public static final MetricId error =
                MetricId.newId("aggregation_supplemental_information_requests_error");
        private static final List<Integer> buckets =
                Arrays.asList(
                        0, 10, 20, 30, 40, 50, 60, 80, 100, 120, 240, 270, 300, 360, 420, 480, 600);

        public static void inc(MetricRegistry registry, MetricId metricId, String clusterId) {
            MetricId metricIdWithLabel = metricId.label(CLUSTER_LABEL, clusterId);
            registry.meter(metricIdWithLabel).inc();
        }

        public static void observe(
                MetricRegistry metricRegistry, MetricId histogram, long duration) {

            metricRegistry.histogram(histogram, buckets).update(duration);
        }
    }

    // Cached accounts have not been sent to system side yet.
    protected Map<String, Pair<Account, AccountFeatures>> allAvailableAccountsByUniqueId;
    // Updated accounts have been sent to System side and has been updated with their stored Tink Id
    protected Map<String, Account> updatedAccountsByTinkId;
    private Set<String> updatedAccountUniqueIds;
    // a collection of account to keep a record of what accounts we should aggregate data after
    // opt-in flow,
    // selecting white listed accounts and eliminating blacklisted accounts
    protected List<Account> accountsToAggregate;
    // a collection of account numbers that the Opt-in user selected during the opt-in flow
    // True or false if system has been requested to process transactions.
    protected boolean isSystemProcessingTransactions;
    protected boolean isWhitelistRefresh;
    protected ControllerWrapper controllerWrapper;

    protected IdentityData identityData;
    private RegulatoryRestrictions regulatoryRestrictions;

    public AgentWorkerContext(
            CredentialsRequest request,
            MetricRegistry metricRegistry,
            CuratorFramework coordinationClient,
            AggregatorInfo aggregatorInfo,
            SupplementalInformationController supplementalInformationController,
            ProviderSessionCacheController providerSessionCacheController,
            ControllerWrapper controllerWrapper,
            String clusterId,
            String appId,
            RegulatoryRestrictions regulatoryRestrictions) {

        this.accountDataCache = new AccountDataCache();
        this.allAvailableAccountsByUniqueId = Maps.newHashMap();
        this.updatedAccountsByTinkId = Maps.newHashMap();
        this.updatedAccountUniqueIds = Sets.newHashSet();
        this.accountsToAggregate = Lists.newArrayList();
        this.psd2PaymentAccountClassifier =
                Psd2PaymentAccountClassifier.createWithMetrics(metricRegistry);

        this.request = request;

        // _Not_ instanciating a SystemService from the ServiceFactory here.
        this.coordinationClient = coordinationClient;
        this.regulatoryRestrictions = regulatoryRestrictions;

        setClusterId(clusterId);
        setAggregatorInfo(aggregatorInfo);
        setAppId(appId);

        if (request.getUser() != null) {
            String locale = request.getUser().getProfile().getLocale();
            this.catalog = Catalog.getCatalog(locale);
            log.info("Catalog created on Locale: " + locale);
        }

        this.setMetricRegistry(metricRegistry);

        this.supplementalInformationController = supplementalInformationController;
        this.providerSessionCacheController = providerSessionCacheController;
        this.controllerWrapper = controllerWrapper;
    }

    @Override
    public void clear() {
        accountDataCache.clear();
        transactionsByAccountBankId.clear();
        allAvailableAccountsByUniqueId.clear();
    }

    @Override
    public Catalog getCatalog() {
        return catalog;
    }

    @Override
    public void processTransactions() {
        Credentials credentials = request.getCredentials();

        List<Transaction> transactions = Lists.newArrayList();

        for (String bankId : transactionsByAccountBankId.keySet()) {
            Optional<Account> accountOpt =
                    getUpdatedAccounts().stream()
                            .filter(a -> Objects.equals(a.getBankId(), bankId))
                            .findFirst();

            if (!accountOpt.isPresent()) {
                if (!isWhitelistRefresh) {
                    log.error(
                            "Account not found in updated Accounts list. "
                                    + "This should not happen and might mean that Agent is not updating all Accounts separately.");
                }

                continue;
            }

            Account account = accountOpt.get();
            if (!shouldAggregateDataForAccount(account)) {
                // Account marked to not aggregate data from.
                // Preferably we would not even download the data but this makes sure
                // we don't process further or store the account's data.
                continue;
            }

            List<Transaction> accountTransactions = transactionsByAccountBankId.get(bankId);

            for (Transaction transaction : accountTransactions) {
                transaction.setAccountId(account.getId());
                transaction.setCredentialsId(request.getCredentials().getId());
                transaction.setUserId(request.getCredentials().getUserId());

                if (!Strings.isNullOrEmpty(transaction.getDescription())) {
                    transaction.setDescription(
                            transaction.getDescription().replace("<", "").replace(">", ""));
                }

                if (transaction.getType() == null) {
                    transaction.setType(TransactionTypes.DEFAULT);
                }
            }

            transactions.addAll(accountTransactions);
        }

        if (credentials.getStatus() != CredentialsStatus.UPDATING) {
            log.warn(
                    String.format(
                            "Status does not warrant transaction processing: %s",
                            credentials.getStatus()));
            return;
        }

        // If fraud credentials, update the statistics and activities.

        if (credentials.getType() == CredentialsTypes.FRAUD) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                            .GenerateStatisticsAndActivitiesRequest
                    generateStatisticsReq =
                            new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                    .GenerateStatisticsAndActivitiesRequest();
            generateStatisticsReq.setUserId(request.getUser().getId());
            generateStatisticsReq.setCredentialsId(request.getCredentials().getId());
            generateStatisticsReq.setUserTriggered(request.isCreate());
            generateStatisticsReq.setMode(StatisticMode.FULL); // To trigger refresh of residences.

            controllerWrapper.generateStatisticsAndActivityAsynchronously(generateStatisticsReq);
            return;
        }

        // Don't update transactions if we didn't get any
        if (transactions.isEmpty()) {
            return;
        }

        // Send the request to process the transactions that we've collected in this batch.

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest
                updateTransactionsRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .UpdateTransactionsRequest();
        updateTransactionsRequest.setTransactions(transactions);
        updateTransactionsRequest.setUser(credentials.getUserId());
        updateTransactionsRequest.setCredentials(credentials.getId());
        updateTransactionsRequest.setCredentialsDataVersion(credentials.getDataVersion());
        updateTransactionsRequest.setUserTriggered(request.isManual());
        updateTransactionsRequest.setRequestTypeFromService(getRequest().getType());
        getRefreshId().ifPresent(updateTransactionsRequest::setAggregationId);

        controllerWrapper.updateTransactionsAsynchronously(updateTransactionsRequest);

        isSystemProcessingTransactions = true;

        // Don't use the queue yet
        //
        //        UpdateTransactionsTask task = new UpdateTransactionsTask();
        //        task.setPayload(updateTransactionsRequest);
        //
        //        taskSubmitter.submit(task);
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String key, long waitFor, TimeUnit unit) {
        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, key));
        SupplementalInformationMetrics.inc(
                getMetricRegistry(), SupplementalInformationMetrics.attempts, getClusterId());
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            // Reset barrier.
            lock.removeBarrier();
            lock.setBarrier();
            log.info(
                    String.format(
                            "Supplemental information request of key %s is waiting for %s %s",
                            key, waitFor, unit));
            if (lock.waitOnBarrier(waitFor, unit)) {
                String supplementalInformation =
                        supplementalInformationController.getSupplementalInformation(key);

                if (Objects.isNull(supplementalInformation)
                        || Objects.equals(supplementalInformation, "null")) {
                    SupplementalInformationMetrics.inc(
                            getMetricRegistry(),
                            SupplementalInformationMetrics.cancelled,
                            getClusterId());
                    log.info(
                            "Supplemental information request was cancelled by client (returned null)");
                    return Optional.empty();
                }
                log.info("Supplemental information response (non-null) has been received");
                SupplementalInformationMetrics.inc(
                        getMetricRegistry(),
                        SupplementalInformationMetrics.finished,
                        getClusterId());
                return Optional.of(supplementalInformation);
            } else {
                log.info("Supplemental information request timed out");
                SupplementalInformationMetrics.inc(
                        getMetricRegistry(),
                        SupplementalInformationMetrics.timedOut,
                        getClusterId());
                // Did not get lock, release anyways and return.
                lock.removeBarrier();
            }

        } catch (Exception e) {
            log.error("Caught exception while waiting for supplemental information", e);
            SupplementalInformationMetrics.inc(
                    getMetricRegistry(), SupplementalInformationMetrics.error, getClusterId());
        } finally {
            // Always clean up the supplemental information
            Credentials credentials = request.getCredentials();
            credentials.setSupplementalInformation(null);
            stopwatch.stop();
            SupplementalInformationMetrics.observe(
                    getMetricRegistry(),
                    SupplementalInformationMetrics.duration,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000);
        }
        log.info("Supplemental information (empty) will be returned");
        return Optional.empty();
    }

    @Override
    public String requestSupplementalInformation(Credentials credentials, boolean wait) {
        if (wait) {
            updateCredentialsExcludingSensitiveInformation(credentials, true);

            Optional<String> supplementalInformation =
                    waitForSupplementalInformation(credentials.getId(), 2, TimeUnit.MINUTES);

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

    public AccountDataCache getAccountDataCache() {
        return accountDataCache;
    }

    private boolean shouldAggregateDataForAccount(Account account) {
        try {
            // TODO: extend filtering by using payment classification information
            // (For now we discard the result as in the beginning we just want to collect metrics)
            Optional<Psd2PaymentAccountClassificationResult> paymentAccountClassification =
                    psd2PaymentAccountClassifier.classify(request.getProvider(), account);
            regulatoryRestrictions.shouldAccountBeRestricted(
                    request, account, paymentAccountClassification);
        } catch (RuntimeException e) {
            log.error("[classifyAsPaymentAccount] Unexpected exception occurred", e);
        }
        return accountsToAggregate.stream()
                .map(Account::getBankId)
                .collect(Collectors.toList())
                .contains(account.getBankId());
    }

    public CredentialsRequest getRequest() {
        return request;
    }

    public Optional<String> getRefreshId() {
        // Defensive. We *Should* only end up in the if iff we're refreshing credentials.
        if (request instanceof RefreshInformationRequest) {
            return Optional.ofNullable(((RefreshInformationRequest) request).getRefreshId());
        }
        return Optional.empty();
    }

    @Override
    public void cacheAccount(Account account, AccountFeatures accountFeatures) {
        accountDataCache.cacheAccount(account);
        accountDataCache.cacheAccountFeatures(account.getBankId(), accountFeatures);

        AccountFeatures accountFeaturesToCache = accountFeatures;

        if (allAvailableAccountsByUniqueId.containsKey(account.getBankId())) {
            // FIXME This whole if-case is a result of having Agents calling cacheAccounts multiple
            // times. Sometimes
            // FIXME with accountFeatures and sometimes without.
            Pair<Account, AccountFeatures> pair =
                    allAvailableAccountsByUniqueId.get(account.getBankId());
            if (accountFeatures.isEmpty() && !pair.second.isEmpty()) {
                accountFeaturesToCache = pair.second;
            }
        }

        allAvailableAccountsByUniqueId.put(
                account.getBankId(), new Pair<>(account, accountFeaturesToCache));
    }

    public Account sendAccountToUpdateService(String uniqueId) {
        Pair<Account, AccountFeatures> pair = allAvailableAccountsByUniqueId.get(uniqueId);

        Account account = pair.first;
        AccountFeatures accountFeatures = pair.second;

        // Only send the accounts once
        if (updatedAccountUniqueIds.contains(uniqueId)) {
            return account;
        }

        if (!shouldAggregateDataForAccount(account)) {
            // Account marked to not aggregate data from.
            // Preferably we would not even download the data but this makes sure
            // we don't process further or store the account's data.
            return account;
        }

        account.setCredentialsId(request.getCredentials().getId());
        account.setUserId(request.getCredentials().getUserId());
        account.setFinancialInstitutionId(request.getProvider().getFinancialInstitutionId());

        // This is to handle legacy agents. Once all legacy agents are gone this can be removed.
        // The logic of adding currency code for next gen agents is done in Account.toSystemAccount
        if (Strings.isNullOrEmpty(account.getCurrencyCode())) {
            account.setCurrencyCode(request.getProvider().getCurrency());
        }

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest
                updateAccountRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .UpdateAccountRequest();

        updateAccountRequest.setUser(request.getCredentials().getUserId());
        updateAccountRequest.setAccount(CoreAccountMapper.fromAggregation(account));
        updateAccountRequest.setAccountFeatures(accountFeatures);
        updateAccountRequest.setCredentialsId(request.getCredentials().getId());
        updateAccountRequest.setCredentialsDataVersion(request.getCredentials().getDataVersion());
        updateAccountRequest.setAvailableBalance(account.getAvailableBalance());
        updateAccountRequest.setCreditLimit(account.getCreditLimit());

        Account updatedAccount;
        try {
            updatedAccount = controllerWrapper.updateAccount(updateAccountRequest);

        } catch (UniformInterfaceException e) {
            log.error(
                    "Account update request failed, response: "
                            + (e.getResponse().hasEntity()
                                    ? e.getResponse().getEntity(String.class)
                                    : ""));
            throw e;
        }

        updatedAccountUniqueIds.add(uniqueId);
        updatedAccountsByTinkId.put(updatedAccount.getId(), updatedAccount);

        return updatedAccount;
    }

    public AccountHolder sendAccountHolderToUpdateService(String tinkId) {
        AccountHolder accountHolder =
                Optional.ofNullable(updatedAccountsByTinkId.get(tinkId))
                        .map(account -> allAvailableAccountsByUniqueId.get(account.getBankId()))
                        .map(account -> account.first)
                        .map(Account::getAccountHolder)
                        .orElse(null);

        if (Objects.isNull(accountHolder)) {
            log.debug("account: " + tinkId + "has no account holder");
            return null;
        }
        accountHolder.setAccountId(tinkId);
        UpdateAccountHolderRequest updateAccountHolderRequest = new UpdateAccountHolderRequest();
        updateAccountHolderRequest.setAccountHolder(accountHolder);
        updateAccountHolderRequest.setAppId(this.getAppId());
        updateAccountHolderRequest.setUserId(request.getCredentials().getUserId());
        try {
            return controllerWrapper.updateAccountHolder(updateAccountHolderRequest);
        } catch (UniformInterfaceException e) {
            log.error(
                    "Account holder update request failed, response: "
                            + (e.getResponse().hasEntity()
                                    ? e.getResponse().getEntity(String.class)
                                    : ""));
            throw e;
        }
    }

    public Account updateAccount(String uniqueId) {
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
        account.setFinancialInstitutionId(request.getProvider().getFinancialInstitutionId());

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest
                updateAccountRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .UpdateAccountRequest();

        updateAccountRequest.setUser(request.getCredentials().getUserId());
        updateAccountRequest.setAccount(CoreAccountMapper.fromAggregation(account));
        updateAccountRequest.setAccountFeatures(accountFeatures);
        updateAccountRequest.setCredentialsId(request.getCredentials().getId());
        updateAccountRequest.setCredentialsDataVersion(request.getCredentials().getDataVersion());
        updateAccountRequest.setAvailableBalance(account.getAvailableBalance());
        updateAccountRequest.setCreditLimit(account.getCreditLimit());

        Account updatedAccount;
        try {
            updatedAccount = controllerWrapper.updateAccount(updateAccountRequest);

        } catch (UniformInterfaceException e) {
            log.error(
                    "Account update request failed, response: "
                            + (e.getResponse().hasEntity()
                                    ? e.getResponse().getEntity(String.class)
                                    : ""));
            throw e;
        }

        updatedAccountsByTinkId.put(updatedAccount.getId(), updatedAccount);

        return updatedAccount;
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doStatusUpdate) {
        updateCredentialsExcludingSensitiveInformation(credentials, doStatusUpdate, false);
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doStatusUpdate, boolean isMigrationUpdate) {
        // Execute any event-listeners.

        for (AgentEventListener eventListener : eventListeners) {
            eventListener.onUpdateCredentialsStatus();
        }

        Optional<String> refreshId = getRefreshId();

        // Clone the credentials here so that we can pass a copy with no
        // secrets back to the system service.

        Credentials credentialsCopy = credentials.clone();
        credentialsCopy.clearSensitiveInformation(request.getProvider());

        se.tink.libraries.credentials.rpc.Credentials coreCredentials =
                CoreCredentialsMapper.fromAggregationCredentials(credentialsCopy);

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest
                updateCredentialsStatusRequest =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .UpdateCredentialsStatusRequest();
        updateCredentialsStatusRequest.setCredentials(coreCredentials);
        updateCredentialsStatusRequest.setUserId(credentials.getUserId());
        updateCredentialsStatusRequest.setUpdateContextTimestamp(doStatusUpdate);
        updateCredentialsStatusRequest.setUserDeviceId(request.getUserDeviceId());
        updateCredentialsStatusRequest.setMigrationUpdate(isMigrationUpdate);
        updateCredentialsStatusRequest.setRequestType(request.getType());
        log.info(
                String.format(
                        "refreshId: %s - Incoming RequestType is %s, outgoing %s",
                        refreshId.orElse("null"),
                        request.getType(),
                        updateCredentialsStatusRequest.getRequestType()));

        refreshId.ifPresent(updateCredentialsStatusRequest::setRefreshId);

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);
    }

    @Override
    public void updateFraudDetailsContent(List<FraudDetailsContent> detailsContents) {
        UpdateFraudDetailsRequest updateFraudRequest = new UpdateFraudDetailsRequest();
        updateFraudRequest.setUserId(request.getUser().getId());
        updateFraudRequest.setDetailsContents(detailsContents);

        controllerWrapper.updateFraudDetails(updateFraudRequest);
    }

    @Override
    public void updateStatus(CredentialsStatus status) {
        updateStatus(status, null);
    }

    @Override
    public void updateStatus(
            final CredentialsStatus status,
            final String statusPayload,
            final boolean statusFromProvider) {
        Credentials credentials = request.getCredentials();
        credentials.setStatus(status);
        credentials.setStatusPayload(statusPayload);

        if (!statusFromProvider) {
            updateCredentialsExcludingSensitiveInformation(credentials, true);
            return;
        }

        if (statusPayload == null) {
            updateCredentialsExcludingSensitiveInformation(credentials, true);
            return;
        }

        if ((status != CredentialsStatus.AUTHENTICATION_ERROR
                && status != CredentialsStatus.TEMPORARY_ERROR)) {
            updateCredentialsExcludingSensitiveInformation(credentials, true);
            return;
        }

        String payload =
                catalog.getString("Error from")
                        + " "
                        + request.getProvider().getDisplayName()
                        + ": \""
                        + statusPayload
                        + "\"";

        credentials.setStatusPayload(payload);

        updateCredentialsExcludingSensitiveInformation(credentials, true);
    }

    @Override
    @Deprecated // Use cacheTransactions instead
    public Account updateTransactions(final Account account, List<Transaction> transactions) {
        // Ensure the account is cached before caching transactions.
        cacheAccount(account);
        cacheTransactions(account.getBankId(), transactions);
        return account;
    }

    @Override
    public void cacheTransactions(@Nonnull String accountUniqueId, List<Transaction> transactions) {
        Preconditions.checkNotNull(
                accountUniqueId); // Necessary until we make @Nonnull throw the exception
        // This crashes if agent is implemented incorrectly. You have to cache Account before you
        // cache Transactions
        Preconditions.checkArgument(allAvailableAccountsByUniqueId.containsKey(accountUniqueId));
        transactionsByAccountBankId.put(accountUniqueId, transactions);

        accountDataCache.cacheTransactions(accountUniqueId, transactions);
    }

    @Override
    public void updateTransferDestinationPatterns(
            Map<Account, List<TransferDestinationPattern>> transferDestinationPatterns) {

        transferDestinationPatterns.forEach(
                (account, patterns) -> {
                    accountDataCache.cacheAccount(account);
                    accountDataCache.cacheTransferDestinationPatterns(
                            account.getBankId(), patterns);
                });

        for (Account account : transferDestinationPatterns.keySet()) {
            if (transferDestinationPatternsByAccount.containsKey(account)) {
                transferDestinationPatternsByAccount
                        .get(account)
                        .addAll(transferDestinationPatterns.get(account));
            } else {
                transferDestinationPatternsByAccount.put(
                        account, transferDestinationPatterns.get(account));
            }
        }
    }

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {}

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
                log.warn(
                        String.format(
                                "Ignoring transfer because it has missing destination: %s",
                                transfer));
                continue;
            }

            if (!destination.isValid()) {
                log.warn(
                        String.format(
                                "Ignoring non-valid transfer with identifier '%s'. Transfer: %s",
                                destination.getIdentifier(), transfer));
                continue;
            }

            // Validation passed.

            this.transfers.add(transfer);
        }
    }

    @Override
    public void cacheIdentityData(IdentityData identityData) {
        this.identityData = identityData;
        if (identityData != null) {
            log.info("Identity data is cached in context");
        } else {
            log.info("Identity data is null, so it will not be cached");
        }
    }

    public se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData
            getAggregationIdentityData() {
        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData
                simplifiedIdentityData =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData();

        LocalDate dateOfBirth = identityData.getDateOfBirth();
        if (Objects.nonNull(dateOfBirth)) {
            simplifiedIdentityData.setDateOfBirth(dateOfBirth.toString());
        }

        simplifiedIdentityData.setName(identityData.getFullName());
        simplifiedIdentityData.setSsn(identityData.getSsn());

        return simplifiedIdentityData;
    }

    @Override
    public void sendIdentityToIdentityAggregatorService() {

        if (identityData == null) {
            log.info("Identity data is null, skipping identity data update request");
            return;
        }

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData
                simplifiedIdentityData = getAggregationIdentityData();

        UpdateIdentityDataRequest updateIdentityDataRequest = new UpdateIdentityDataRequest();
        updateIdentityDataRequest.setIdentityData(simplifiedIdentityData);
        updateIdentityDataRequest.setProviderName(request.getProvider().getName());
        updateIdentityDataRequest.setUserId(request.getUser().getId());

        try {
            controllerWrapper.updateIdentityData(updateIdentityDataRequest);
            log.info("Identity data is successfully updated");
        } catch (UniformInterfaceException e) {
            log.error(
                    "Identity update request failed, response: "
                            + (e.getResponse().hasEntity()
                                    ? e.getResponse().getEntity(String.class)
                                    : ""));
            throw e;
        }
    }

    @Override
    public String getProviderSessionCache() {
        return providerSessionCacheController.getProviderSessionCache(
                getAppId(), request.getProvider().getFinancialInstitutionId());
    }

    @Override
    public void setProviderSessionCache(String value, int expiredTimeInSeconds) {
        providerSessionCacheController.setProviderSessionCache(
                getAppId(),
                request.getProvider().getFinancialInstitutionId(),
                value,
                expiredTimeInSeconds);
    }
}
