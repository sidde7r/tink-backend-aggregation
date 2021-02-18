package se.tink.backend.aggregation.workers.operation;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;
import io.dropwizard.lifecycle.Managed;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.agents.utils.mappers.CoreAccountHolderMapper;
import se.tink.backend.aggregation.agents.utils.mappers.CoreAccountMapper;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountHolderRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.locks.BarrierName;
import se.tink.backend.aggregationcontroller.v1.rpc.credentialsservice.UpdateCredentialsSupplementalInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.enums.StatisticMode;
import se.tink.libraries.giro.validation.LuhnCheck;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.transfer.rpc.Transfer;

public class AgentWorkerContext extends AgentContext implements Managed {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final MetricId SUSPICIOUS_NUMBER_SERIES =
            MetricId.newId("aggregation_account_suspicious_number_series");

    private Catalog catalog;
    protected CuratorFramework coordinationClient;
    protected CredentialsRequest request;
    private final AccountDataCache accountDataCache;

    protected List<Transfer> transfers = Lists.newArrayList();
    protected List<AgentEventListener> eventListeners = Lists.newArrayList();
    private SupplementalInformationController supplementalInformationController;
    private ProviderSessionCacheController providerSessionCacheController;
    protected final String correlationId;
    protected final AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;

    private static class SupplementalInformationMetrics {
        private static final String CLUSTER_LABEL = "client_cluster";
        public static final MetricId duration =
                MetricId.newId("aggregation_supplemental_information_seconds");
        public static final MetricId attempts =
                MetricId.newId("aggregation_supplemental_information_requests_started");
        public static final MetricId finished =
                MetricId.newId("aggregation_supplemental_information_requests_finished");
        public static final MetricId finished_with_empty =
                MetricId.newId("aggregation_supplemental_information_requests_finished_empty");
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

    // a collection of account numbers that the Opt-in user selected during the opt-in flow
    // True or false if system has been requested to process transactions.
    protected boolean isSystemProcessingTransactions;
    protected ControllerWrapper controllerWrapper;

    protected IdentityData identityData;

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
            String correlationId,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer) {

        this.accountDataCache = new AccountDataCache();
        this.correlationId = correlationId;

        this.request = request;

        // _Not_ instanciating a SystemService from the ServiceFactory here.
        this.coordinationClient = coordinationClient;
        this.accountInformationServiceEventsProducer = accountInformationServiceEventsProducer;

        setClusterId(clusterId);
        setAggregatorInfo(aggregatorInfo);
        setAppId(appId);

        if (request.getUser() != null) {
            String locale = request.getUser().getProfile().getLocale();
            this.catalog = Catalog.getCatalog(locale);
            logger.info("[AgentWorkerContext] Catalog created on Locale: {}", locale);
        } else {
            logger.warn(
                    "[AgentWorkerContext] Could not get locale. AppId {} CorrelationId {}",
                    appId,
                    correlationId);
        }

        this.setMetricRegistry(metricRegistry);

        this.supplementalInformationController = supplementalInformationController;
        this.providerSessionCacheController = providerSessionCacheController;
        this.controllerWrapper = controllerWrapper;
    }

    @Override
    public void clear() {
        accountDataCache.clear();
    }

    @Override
    public Catalog getCatalog() {
        return catalog;
    }

    @Override
    public void processTransactions() {
        Credentials credentials = request.getCredentials();
        if (hasStatusDifferentThanUpdating(credentials)) {
            return;
        }

        if (credentials.getType() == CredentialsTypes.FRAUD) {
            updateStatisticsAndActivities(credentials);
            return;
        }

        List<Transaction> transactionsToProcess = accountDataCache.getTransactionsToBeProcessed();
        if (transactionsToProcess.isEmpty()) {
            logAboutTransactionsEmpty(credentials);
            return;
        }

        List<Transaction> transactionsWithDate =
                prepareTransactionsForUpdateRequest(credentials, transactionsToProcess);

        if (transactionsWithDate.isEmpty()) {
            logAboutTransactionsEmpty(credentials);
            return;
        }

        UpdateTransactionsRequest updateTransactionsRequest =
                createUpdateTransactionsRequest(credentials, transactionsWithDate);

        controllerWrapper.updateTransactionsAsynchronously(updateTransactionsRequest);

        isSystemProcessingTransactions = true;
    }

    private boolean hasStatusDifferentThanUpdating(Credentials credentials) {
        if (credentials.getStatus() != CredentialsStatus.UPDATING) {
            logger.warn(
                    "Status does not warrant transaction processing: {}", credentials.getStatus());
            return true;
        }
        return false;
    }

    private void updateStatisticsAndActivities(Credentials credentials) {
        se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                        .GenerateStatisticsAndActivitiesRequest
                generateStatisticsReq =
                        new se.tink.backend.aggregation.aggregationcontroller.v1.rpc
                                .GenerateStatisticsAndActivitiesRequest();
        generateStatisticsReq.setUserId(request.getUser().getId());
        generateStatisticsReq.setCredentialsId(credentials.getId());
        generateStatisticsReq.setUserTriggered(request.isCreate());
        generateStatisticsReq.setMode(StatisticMode.FULL); // To trigger refresh of residences.

        controllerWrapper.generateStatisticsAndActivityAsynchronously(generateStatisticsReq);
    }

    private List<Transaction> prepareTransactionsForUpdateRequest(
            Credentials credentials, List<Transaction> transactionsToProcess) {
        // Update each transaction with information about the credentials and user as well as
        // additional formatting.
        logger.info(
                "Processing {} transactions. [UserId:{} CredentialsId:{}]",
                transactionsToProcess.size(),
                credentials.getUserId(),
                credentials.getId());

        List<Transaction> transactionsWithDate =
                transactionsToProcess.stream()
                        .filter(transaction -> transaction.getDate() != null)
                        .peek(
                                transaction ->
                                        enrichTransactionWithCredentials(transaction, credentials))
                        .collect(Collectors.toList());

        logger.info(
                "Processing {} transactions after null date filtering. [UserId:{} CredentialsId:{}]",
                transactionsWithDate.size(),
                credentials.getUserId(),
                credentials.getId());

        if (transactionsToProcess.size() != transactionsWithDate.size()) {
            logger.warn("There were some transactions without date, which where filtered.");
        }

        return transactionsWithDate;
    }

    private void enrichTransactionWithCredentials(
            Transaction transaction, Credentials credentials) {
        transaction.setCredentialsId(credentials.getId());
        transaction.setUserId(credentials.getUserId());
        if (!Strings.isNullOrEmpty(transaction.getDescription())) {
            transaction.setDescription(
                    transaction.getDescription().replace("<", "").replace(">", ""));
        }
        if (transaction.getType() == null) {
            transaction.setType(TransactionTypes.DEFAULT);
        }
    }

    private UpdateTransactionsRequest createUpdateTransactionsRequest(
            Credentials credentials, List<Transaction> transactionsWithoutDate) {
        UpdateTransactionsRequest updateTransactionsRequest = new UpdateTransactionsRequest();
        updateTransactionsRequest.setTransactions(transactionsWithoutDate);
        updateTransactionsRequest.setUser(credentials.getUserId());
        updateTransactionsRequest.setCredentials(credentials.getId());
        updateTransactionsRequest.setUserTriggered(request.isManual());
        updateTransactionsRequest.setMarket(request.getProvider().getMarket());
        updateTransactionsRequest.setRequestType(getRequest().getType());
        updateTransactionsRequest.setOperationId(request.getOperationId());
        getRefreshId().ifPresent(updateTransactionsRequest::setAggregationId);
        return updateTransactionsRequest;
    }

    private void logAboutTransactionsEmpty(Credentials credentials) {
        logger.info(
                "Skipping transaction processing because we don't have any [UserId:{} CredentialsId:{}]",
                credentials.getUserId(),
                credentials.getId());
    }

    @Override
    public Optional<String> waitForSupplementalInformation(
            String mfaId, long waitFor, TimeUnit unit) {
        DistributedBarrier lock =
                new DistributedBarrier(
                        coordinationClient,
                        BarrierName.build(BarrierName.Prefix.SUPPLEMENTAL_INFORMATION, mfaId));
        SupplementalInformationMetrics.inc(
                getMetricRegistry(), SupplementalInformationMetrics.attempts, getClusterId());
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            // Reset barrier.
            lock.removeBarrier();
            lock.setBarrier();
            logger.info(
                    "Supplemental information request of key {} is waiting for {} {}",
                    mfaId,
                    waitFor,
                    unit);
            logger.info(
                    "[Supplemental Information] Credential Status: {}",
                    Optional.ofNullable(request.getCredentials())
                            .map(Credentials::getStatus)
                            .orElse(null));
            if (lock.waitOnBarrier(waitFor, unit)) {
                String result = supplementalInformationController.getSupplementalInformation(mfaId);

                if (Objects.isNull(result) || Objects.equals(result, "null")) {
                    SupplementalInformationMetrics.inc(
                            getMetricRegistry(),
                            SupplementalInformationMetrics.cancelled,
                            getClusterId());
                    logger.info(
                            "Supplemental information request was cancelled by client (returned null)");
                    return Optional.empty();
                }

                if ("".equals(result)) {
                    logger.info(
                            "Supplemental information response (empty!) has been received for provider: {}, from appid: {}",
                            request.getProvider().getName(),
                            getAppId());
                    SupplementalInformationMetrics.inc(
                            getMetricRegistry(),
                            SupplementalInformationMetrics.finished_with_empty,
                            getClusterId());
                } else {
                    logger.info(
                            "Supplemental information response (non-null &  non-empty) has been received");
                    SupplementalInformationMetrics.inc(
                            getMetricRegistry(),
                            SupplementalInformationMetrics.finished,
                            getClusterId());
                }

                return Optional.of(result);
            } else {
                logger.info("Supplemental information request timed out");
                SupplementalInformationMetrics.inc(
                        getMetricRegistry(),
                        SupplementalInformationMetrics.timedOut,
                        getClusterId());
                // Did not get lock, release anyways and return.
                lock.removeBarrier();
            }

        } catch (Exception e) {
            logger.error("Caught exception while waiting for supplemental information", e);
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
        logger.info("Supplemental information (empty) will be returned");
        return Optional.empty();
    }

    @Override
    public void requestSupplementalInformation(String mfaId, Credentials credentials) {
        supplementalInteractionCounter.inc();

        // Execute any event-listeners; this tells the signable operation to update status
        for (AgentEventListener eventListener : eventListeners) {
            eventListener.onUpdateCredentialsStatus();
        }

        UpdateCredentialsSupplementalInformationRequest suppInfoRequest =
                new UpdateCredentialsSupplementalInformationRequest();
        suppInfoRequest.setMfaId(mfaId);
        suppInfoRequest.setCredentialsId(credentials.getId());
        suppInfoRequest.setStatus(credentials.getStatus());
        suppInfoRequest.setSupplementalInformation(credentials.getSupplementalInformation());
        suppInfoRequest.setUserId(credentials.getUserId());
        suppInfoRequest.setRequestType(getRequest().getType());
        suppInfoRequest.setOperationId(getRequest().getOperationId());
        suppInfoRequest.setManual(getRequest().isManual());
        suppInfoRequest.setRefreshId(getRefreshId().orElse(null));

        controllerWrapper.updateSupplementalInformation(suppInfoRequest);
    }

    public AccountDataCache getAccountDataCache() {
        return accountDataCache;
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
    }

    // Note: this method should not be called simultaneously because that may cause unwanted side
    // effects especially during the PSD2 migration process
    public Account sendAccountToUpdateService(String bankAccountId) {
        Optional<AccountData> optionalAccountData =
                accountDataCache.getFilteredAccountDataByBankAccountId(bankAccountId);
        if (!optionalAccountData.isPresent()) {
            logger.warn(
                    "Trying to send a filtered or non-existent Account to update service! Agents should not do this on their own.");
            return null;
        }

        AccountData accountData = optionalAccountData.get();
        if (accountData.isProcessed()) {
            // Already updated/processed, do not do it twice.
            logger.info(
                    "skip updating account from sendAccountToUpdateService as the account data is processed.");
            return accountData.getAccount();
        }

        Account account = accountData.getAccount();
        AccountFeatures accountFeatures = accountData.getAccountFeatures();

        account.setCredentialsId(request.getCredentials().getId());
        account.setUserId(request.getCredentials().getUserId());
        account.setFinancialInstitutionId(request.getProvider().getFinancialInstitutionId());

        measureSuspiciousNumberSeries("accountnumber", account.getAccountNumber());
        measureSuspiciousNumberSeries("bankid", account.getBankId());

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
        updateAccountRequest.setAvailableBalance(account.getAvailableBalance());
        updateAccountRequest.setCreditLimit(account.getCreditLimit());
        updateAccountRequest.setOperationId(request.getOperationId());
        updateAccountRequest.setCorrelationId(getCorrelationId());

        Optional<se.tink.backend.aggregationcontroller.v1.rpc.accountholder.AccountHolder>
                acAccountHolder =
                        CoreAccountHolderMapper.fromAggregation(account.getAccountHolder());
        if (acAccountHolder.isPresent()) {
            updateAccountRequest.setAccountHolder(acAccountHolder.get());
        }

        Account updatedAccount;
        try {
            logger.info("Account update from sendAccountToUpdateService");
            updatedAccount = controllerWrapper.updateAccount(updateAccountRequest);

        } catch (UniformInterfaceException e) {
            logger.error(
                    "Account update request failed, response: {}",
                    (e.getResponse().hasEntity() ? e.getResponse().getEntity(String.class) : ""));
            throw e;
        }

        accountDataCache.setProcessedTinkAccountId(
                updatedAccount.getBankId(), updatedAccount.getId());

        return updatedAccount;
    }

    private void measureSuspiciousNumberSeries(String label, String numberSeries) {
        if (Strings.isNullOrEmpty(numberSeries)) {
            return;
        }

        String cleaned = numberSeries.replace("-", "").replace(" ", "");

        // 12-19 digits, and luhn validation comes from:
        // https://en.wikipedia.org/wiki/Payment_card_number
        if (StringUtils.isNumeric(cleaned)
                && cleaned.length() >= 12
                && cleaned.length() <= 19
                && LuhnCheck.isLastCharCorrectLuhnMod10Check(cleaned)) {

            String agent = request.getProvider().getClassName();
            getMetricRegistry()
                    .meter(SUSPICIOUS_NUMBER_SERIES.label("agent", agent).label("label", label))
                    .inc();
            logger.warn(
                    "Found suspicous number series ({}) from {}, credentialsId: {}.",
                    label,
                    agent,
                    request.getCredentials().getId());
        }
    }

    public AccountHolder sendAccountHolderToUpdateService(Account processedAccount) {
        String tinkAccountId = processedAccount.getId();
        AccountHolder accountHolder = processedAccount.getAccountHolder();
        if (Objects.isNull(accountHolder)) {
            logger.debug("tinkAccountId: {} has no account holder", tinkAccountId);
            return null;
        }
        accountHolder.setAccountId(tinkAccountId);
        UpdateAccountHolderRequest updateAccountHolderRequest = new UpdateAccountHolderRequest();
        updateAccountHolderRequest.setAccountHolder(accountHolder);
        updateAccountHolderRequest.setAppId(this.getAppId());
        updateAccountHolderRequest.setUserId(request.getCredentials().getUserId());
        try {
            return controllerWrapper.updateAccountHolder(updateAccountHolderRequest);
        } catch (UniformInterfaceException e) {
            logger.error(
                    "Account holder update request failed, response: {}",
                    (e.getResponse().hasEntity() ? e.getResponse().getEntity(String.class) : ""));
            throw e;
        }
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doStatusUpdate) {
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
        updateCredentialsStatusRequest.setRequestType(request.getType());
        updateCredentialsStatusRequest.setOperationId(request.getOperationId());
        logger.info(
                "refreshId: {} - Incoming RequestType is {}, outgoing {}",
                refreshId.orElse("null"),
                request.getType(),
                updateCredentialsStatusRequest.getRequestType());

        refreshId.ifPresent(updateCredentialsStatusRequest::setRefreshId);

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);
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
                        + catalog.getString(statusPayload)
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
    }

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {}

    @Override
    public List<Account> getUpdatedAccounts() {
        return Lists.newArrayList(accountDataCache.getProcessedAccounts());
    }

    @Override
    public void updateEinvoices(List<Transfer> transfers) {
        if (transfers.isEmpty()) {
            return;
        }

        for (Transfer transfer : transfers) {

            AccountIdentifier destination = transfer.getDestination();
            if (destination != null && destination.isValid()) {
                this.transfers.add(transfer);
            } else {
                logInfoAboutInvalidTransferDestination(transfer, destination);
            }
        }
    }

    private void logInfoAboutInvalidTransferDestination(
            Transfer transfer, AccountIdentifier destination) {
        if (destination == null) {
            logger.warn("Ignoring transfer because it has missing destination: {}", transfer);
        }

        if (destination != null && !destination.isValid()) {
            logger.warn(
                    "Ignoring non-valid transfer with identifier {}. Transfer: {}",
                    destination.getIdentifier(),
                    transfer);
        }
    }

    @Override
    public void cacheIdentityData(IdentityData identityData) {
        this.identityData = identityData;
        if (identityData != null) {
            logger.info("Identity data is cached in context");
        } else {
            logger.info("Identity data is null, so it will not be cached");
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
            logger.info("Identity data is null, skipping identity data update request");
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
            logger.info("Identity data is successfully updated");
        } catch (UniformInterfaceException e) {
            logger.error(
                    "Identity update request failed, response: {}",
                    (e.getResponse().hasEntity() ? e.getResponse().getEntity(String.class) : ""));
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

    public String getCorrelationId() {
        return correlationId;
    }
}
