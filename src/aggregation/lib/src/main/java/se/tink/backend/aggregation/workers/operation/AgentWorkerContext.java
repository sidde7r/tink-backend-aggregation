package se.tink.backend.aggregation.workers.operation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;
import io.dropwizard.lifecycle.Managed;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import no.finn.unleash.UnleashContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
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
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.entity.HttpJsonLogMetaEntity;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.raw_data_events.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorageImpl;
import se.tink.backend.aggregation.workers.operation.supplemental_information_requesters.LegacySupplementalInformationWaiter;
import se.tink.backend.aggregation.workers.operation.supplemental_information_requesters.NxgenSupplementalInformationWaiter;
import se.tink.backend.aggregation.workers.operation.supplemental_information_requesters.SupplementalInformationDemander;
import se.tink.backend.aggregation.workers.operation.supplemental_information_requesters.SupplementalInformationWaiter;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.giro.validation.LuhnCheck;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.se.tink.libraries.har_logger.src.logger.HarLogCollector;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.model.UnleashContextWrapper;

public class AgentWorkerContext extends AgentContext implements Managed {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String AGENT = "agent";
    private static final MetricId SUSPICIOUS_NUMBER_SERIES =
            MetricId.newId("aggregation_account_suspicious_number_series");
    private static final MetricId CREDENTIALS_STATUS_CHANGES_WITHOUT_ERRORS =
            MetricId.newId("aggregation_credentials_status_changes_without_errors");
    private static final MetricId RESULTING_ERRORS = MetricId.newId("aggregation_resulting_errors");

    private static final Set<CredentialsStatus> ERROR_STATUSES =
            ImmutableSet.of(
                    CredentialsStatus.TEMPORARY_ERROR,
                    CredentialsStatus.AUTHENTICATION_ERROR,
                    CredentialsStatus.UNCHANGED);

    private Catalog catalog;
    private final CuratorFramework coordinationClient;
    protected CredentialsRequest request;
    private final AccountDataCache accountDataCache;

    protected List<Transfer> transfers = Lists.newArrayList();
    protected List<AgentEventListener> eventListeners = Lists.newArrayList();
    private final SupplementalInformationController supplementalInformationController;
    private final ProviderSessionCacheController providerSessionCacheController;
    protected final AccountInformationServiceEventsProducer accountInformationServiceEventsProducer;
    private final RequestStatusManager requestStatusManager;

    // a collection of account numbers that the Opt-in user selected during the opt-in flow
    // True or false if system has been requested to process transactions.
    protected boolean isSystemProcessingTransactions;
    protected ControllerWrapper controllerWrapper;
    protected IdentityData identityData;
    protected RawBankDataEventAccumulator rawBankDataEventAccumulator;

    private final String operationName;

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
            String operationName,
            String correlationId,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer,
            UnleashClient unleashClient,
            RequestStatusManager requestStatusManager,
            RawBankDataEventAccumulator rawBankDataEventAccumulator) {
        logger.debug("Starting constructing AgentWorkerContext");

        this.accountDataCache = new AccountDataCache();
        this.request = request;

        // _Not_ instantiating a SystemService from the ServiceFactory here.
        this.coordinationClient = coordinationClient;
        this.accountInformationServiceEventsProducer = accountInformationServiceEventsProducer;

        setRawBankDataEventAccumulator(rawBankDataEventAccumulator);
        setClusterId(clusterId);
        setAggregatorInfo(aggregatorInfo);
        setAppId(appId);
        setProviderId(request.getProvider().getName());
        setCorrelationId(correlationId);

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
        this.setUnleashClient(unleashClient);
        this.setAgentTemporaryStorage(new AgentTemporaryStorageImpl());

        this.supplementalInformationController = supplementalInformationController;
        this.providerSessionCacheController = providerSessionCacheController;
        this.controllerWrapper = controllerWrapper;
        this.requestStatusManager = requestStatusManager;

        this.operationName = operationName;

        RawHttpTrafficLogger.inMemoryLogger().ifPresent(this::setRawHttpTrafficLogger);
        HttpJsonLogMetaEntity logMetaEntity =
                HttpJsonLogMetaEntity.builder()
                        .agentName(request.getProvider().getClassName())
                        .providerName(request.getProvider().getName())
                        .appId(appId)
                        .clusterId(clusterId)
                        .credentialsId(request.getCredentials().getId())
                        .userId(request.getCredentials().getUserId())
                        .requestId(request.getRequestId())
                        .operation(operationName)
                        .build();
        setJsonHttpTrafficLogger(new JsonHttpTrafficLogger(logMetaEntity));

        final Map<String, String> logMetaData =
                SerializationUtils.deserializeFromString(
                        SerializationUtils.serializeToString(logMetaEntity),
                        new TypeReference<Map<String, String>>() {});
        setHarLogCollector(new HarLogCollector(logMetaData));
        logger.debug("Finished constructing AgentWorkerContext");
    }

    public String getOperationName() {
        return operationName;
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
        updateTransactionsRequest.setUserTriggered(request.isUserPresent());
        updateTransactionsRequest.setMarket(request.getProvider().getMarket());
        updateTransactionsRequest.setRequestType(getRequest().getType());
        updateTransactionsRequest.setOperationId(request.getOperationId());
        updateTransactionsRequest.setConsentId(request.getConsentId());
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
            String mfaId, long waitFor, TimeUnit unit, String initiator) {
        SupplementalInformationWaiter supplementalInformationWaiter;
        if (isSupplementalInformationWaitingAbortFeatureEnabled()) {
            supplementalInformationWaiter =
                    new NxgenSupplementalInformationWaiter(
                            getMetricRegistry(),
                            request,
                            coordinationClient,
                            getClusterId(),
                            getAppId(),
                            supplementalInformationController,
                            requestStatusManager);
        } else {
            supplementalInformationWaiter =
                    new LegacySupplementalInformationWaiter(
                            getMetricRegistry(),
                            request,
                            coordinationClient,
                            getClusterId(),
                            getAppId(),
                            supplementalInformationController);
        }
        Optional<String> market =
                Optional.of(request).map(CredentialsRequest::getProvider).map(Provider::getMarket);
        Optional<String> maybeSupplementalInformation =
                supplementalInformationWaiter.waitForSupplementalInformation(
                        mfaId, waitFor, unit, initiator, market.orElse("UNKNOWN"));

        if (maybeSupplementalInformation.isPresent()) {
            Map<String, String> supplementalInfoData =
                    SerializationUtils.deserializeFromString(
                            maybeSupplementalInformation.get(),
                            new TypeReference<Map<String, String>>() {});
            if (supplementalInfoData == null) {
                logger.error(
                        "Error while deserializing supplemental information, we will not be able to mask supplemental information data");
            } else {
                getLogMasker().addNewSensitiveValuesToMasker(supplementalInfoData.values());
            }
        }

        return maybeSupplementalInformation;
    }

    @Override
    public void requestSupplementalInformation(String mfaId, Credentials credentials) {
        new SupplementalInformationDemander(
                        supplementalInteractionCounter,
                        getMetricRegistry(),
                        request,
                        eventListeners,
                        getRefreshId().orElse(null),
                        controllerWrapper)
                .requestSupplementalInformation(mfaId, credentials);
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

        // Don't measure suspicious number series for test providers as some return mocked account
        // numbers that get flagged as suspicious.
        if (!request.getProvider().getType().isTestProvider()) {
            measureSuspiciousNumberSeries("accountnumber", account.getAccountNumber());
            measureSuspiciousNumberSeries("bankid", account.getBankId());
        }

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
        acAccountHolder.ifPresent(updateAccountRequest::setAccountHolder);

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

    @Override
    public Account updateAccountMetaData(String accountId, String newBankId) {
        Account updatedAccount;
        try {
            logger.info("Updating bankId for account {}", accountId);
            updatedAccount = controllerWrapper.updateAccountMetaData(accountId, newBankId);

        } catch (UniformInterfaceException e) {
            logger.error(
                    "Account metadata update request failed, response: {}",
                    (e.getResponse().hasEntity() ? e.getResponse().getEntity(String.class) : ""));
            throw e;
        }

        // update in data cache
        accountDataCache.updateAccountBankId(accountId, newBankId);
        return updatedAccount;
    }

    private void measureSuspiciousNumberSeries(String label, String numberSeries) {
        if (Strings.isNullOrEmpty(numberSeries)) {
            return;
        }

        String cleaned = numberSeries.replace("-", "").replace(" ", "");

        // 15-16 digits, and luhn validation comes from:
        // https://en.wikipedia.org/wiki/Payment_card_number
        if (StringUtils.isNumeric(cleaned)
                && cleaned.length() >= 15
                && cleaned.length() <= 16
                && LuhnCheck.isLastCharCorrectLuhnMod10Check(cleaned)
                // Starts with 3, 4, 5 or 6, the MII digits of Amex, Visa, MC and some other
                // banking issuers. Added to minimise false positives.
                && cleaned.matches("^([3456]).*")) {

            String agent = request.getProvider().getClassName();
            getMetricRegistry()
                    .meter(SUSPICIOUS_NUMBER_SERIES.label(AGENT, agent).label("label", label))
                    .inc();
            logger.warn(
                    "Found suspicious number series ({}) from {}, credentialsId: {}.",
                    label,
                    agent,
                    request.getCredentials().getId());
        }
    }

    @Override
    public void updateCredentialsExcludingSensitiveInformation(
            Credentials credentials, boolean doStatusUpdate) {
        // Execute any event-listeners.

        for (AgentEventListener eventListener : eventListeners) {
            eventListener.onUpdateCredentialsStatus();
        }

        if (ERROR_STATUSES.contains(credentials.getStatus())) {
            String agent = request.getProvider().getClassName();
            getMetricRegistry()
                    .meter(
                            CREDENTIALS_STATUS_CHANGES_WITHOUT_ERRORS
                                    .label(AGENT, agent)
                                    .label("status", credentials.getStatus().toString()))
                    .inc();
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
        updateCredentialsStatusRequest.setConsentId(request.getConsentId());

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
        updateStatus(status, null, true);
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
    public void updateStatusWithError(
            CredentialsStatus status, String statusPayload, ConnectivityError error) {

        getMetricRegistry()
                .meter(
                        RESULTING_ERRORS
                                .label(AGENT, request.getProvider().getClassName())
                                .label("type", error.getType().name())
                                .label("reason", error.getDetails().getReason())
                                .label("status", status.name()))
                .inc();

        Credentials credentials = request.getCredentials();
        credentials.setStatus(status);
        credentials.setStatusPayload(statusPayload);

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
        updateCredentialsStatusRequest.setUpdateContextTimestamp(true);
        updateCredentialsStatusRequest.setUserDeviceId(request.getUserDeviceId());
        updateCredentialsStatusRequest.setRequestType(request.getType());
        updateCredentialsStatusRequest.setOperationId(request.getOperationId());
        updateCredentialsStatusRequest.setConsentId(request.getConsentId());
        refreshId.ifPresent(updateCredentialsStatusRequest::setRefreshId);
        updateCredentialsStatusRequest.setDetailedError(error);

        controllerWrapper.updateCredentials(updateCredentialsStatusRequest);
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
    public void start() {
        // nop
    }

    @Override
    public void stop() {
        agentTemporaryStorage.clear();
    }

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

    private boolean isSupplementalInformationWaitingAbortFeatureEnabled() {
        String appId = getAppId();
        String credentialsId = request.getCredentials().getId();
        boolean isUserPresent = request.getUserAvailability().isUserPresent();
        return isUserPresent
                && getUnleashClient()
                        .isToggleEnabled(
                                Toggle.of("supplemental-information-waiting-abort")
                                        .unleashContextWrapper(
                                                UnleashContextWrapper.builder()
                                                        .unleashContextBuilder(
                                                                UnleashContext.builder()
                                                                        .userId(appId)
                                                                        .sessionId(credentialsId))
                                                        .build())
                                        .build());
    }
}
