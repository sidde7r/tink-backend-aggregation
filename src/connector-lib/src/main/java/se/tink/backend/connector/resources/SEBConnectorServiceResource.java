package se.tink.backend.connector.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.common.config.ConnectorConfiguration;
import se.tink.backend.common.config.FlagsConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.mapper.CoreTransactionMapper;
import se.tink.backend.common.repository.cassandra.CheckpointRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.common.utils.NotificationUtils;
import se.tink.backend.connector.api.SEBConnectorService;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.rpc.seb.AccountEntity;
import se.tink.backend.connector.rpc.seb.AccountListEntity;
import se.tink.backend.connector.rpc.seb.DeleteTransactionAccountContainer;
import se.tink.backend.connector.rpc.seb.DeleteTransactionAccountEntity;
import se.tink.backend.connector.rpc.seb.PartnerAccountPayload;
import se.tink.backend.connector.rpc.seb.RollbackRequest;
import se.tink.backend.connector.rpc.seb.TransactionAccountContainer;
import se.tink.backend.connector.rpc.seb.TransactionAccountEntity;
import se.tink.backend.connector.rpc.seb.UserEntity;
import se.tink.backend.connector.util.handler.TransactionIngestionHandler;
import se.tink.backend.core.Account;
import se.tink.backend.core.Checkpoint;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Market;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserState;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.seb.utils.SEBUtils;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.CheckpointRollbackRequest;
import se.tink.backend.system.rpc.DeleteTransactionRequest;
import se.tink.backend.system.rpc.SendNotificationsRequest;
import se.tink.backend.system.rpc.TransactionToDelete;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;
import se.tink.backend.system.tasks.CheckpointRollbackTask;
import se.tink.backend.system.tasks.DeleteTransactionTask;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class SEBConnectorServiceResource extends ConnectorServiceResource implements SEBConnectorService {
    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private UserRepository userRepository;
    private UserStateRepository userStateRepository;
    private final CheckpointRepository checkpointRepository;

    private Supplier<List<Market>> marketSupplier;
    private final ServiceConfiguration serviceConfiguration;
    private final Cluster cluster;
    private final TransactionIngestionHandler transactionIngestionHandler;
    private FlagsConfiguration flagsConfiguration;

    private final static LogUtils log = new LogUtils(SEBConnectorServiceResource.class);
    private final static ObjectMapper mapper = new ObjectMapper();

    private static final MetricId TRANSACTION_TOPIC_METRIC = MetricId.newId("tink_transaction_topic");

    @Inject
    public SEBConnectorServiceResource(CheckpointRepository checkpointRepository, UserRepository userRepository,
            UserStateRepository userStateRepository, CredentialsRepository credentialsRepository,
            AccountRepository accountRepository, MarketRepository marketRepository, MetricRegistry metricRegistry,
            SystemServiceFactory systemServiceFactory, TaskSubmitter taskSubmitter,
            ServiceConfiguration serviceConfiguration, Cluster cluster, ConnectorConfiguration connectorConfiguration) {
        super(metricRegistry, systemServiceFactory, taskSubmitter);

        this.checkpointRepository = checkpointRepository;
        this.userRepository = userRepository;
        this.userStateRepository = userStateRepository;
        this.credentialsRepository = credentialsRepository;
        this.accountRepository = accountRepository;
        this.marketSupplier = Suppliers.memoizeWithExpiration(
                () -> Collections.unmodifiableList(marketRepository.findAll()), 30, TimeUnit.MINUTES);
        this.serviceConfiguration = serviceConfiguration;
        this.cluster = cluster;
        this.flagsConfiguration = connectorConfiguration.getFlags();

        this.transactionIngestionHandler = new TransactionIngestionHandler(serviceConfiguration.getTaskQueue(),
                taskSubmitter);
    }

    @Override
    @Timed
    public void accounts(String externalUserId, AccountListEntity accountListEntity) {
        if (!accountListEntity.isValid(externalUserId)) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        final User user = validateUser(externalUserId);
        final Credentials credentials = validateCredentials(user);

        log.info(credentials, String.format("Received %d accounts.", accountListEntity.getAccounts().size()));

        for (AccountEntity accountEntity : accountListEntity.getAccounts()) {
            Account account = SEBUtils.createAccount(accountEntity, user, credentials);
            updateAccount(account);
            log.debug(credentials, "Updating Account with id: " + account.getId());
        }
    }

    @Override
    @Timed
    public void sendNotification(String externalUserId, Notification notification) {
        logNotification(notification);
        final User user = validateUser(externalUserId);

        notification.setUserId(user.getId());

        log.info(user.getId(), "Sending notification with ID=" + notification.getId());

        boolean encrypted = NotificationUtils.shouldSendEncrypted(cluster);

        final SendNotificationsRequest sendNotificationsRequest = new SendNotificationsRequest();
        sendNotificationsRequest.addUserNotification(user, notification, encrypted);

        systemServiceFactory.getNotificationGatewayService().sendNotificationsAsynchronously(sendNotificationsRequest);
    }

    @Override
    public void transactions(String externalUserId, final TransactionAccountContainer container) {
        if (container == null || !container.isValid(externalUserId)) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        final User user = validateUser(externalUserId);
        final Credentials credentials = validateCredentials(user);
        final ImmutableMap<String, Account> accountsByExternalId = Maps.uniqueIndex(getAccounts(user, credentials),
                Account::getBankId);
        final List<Transaction> transactions = Lists.newArrayList();

        for (TransactionAccountEntity transactionAccount : container.getTransactionAccounts()) {
            final String externalAccountId = transactionAccount.getExternalId();
            final Account account = accountsByExternalId.get(externalAccountId);

            if (account == null) {
                log.info(user.getId(), credentials.getId(),
                        String.format("No account found for externalAccountId:%s.", externalAccountId));
                HttpResponseHelper.error(Status.BAD_REQUEST);
            }

            setBalance(account, transactionAccount.getDisposableAmount(), transactionAccount.getBalance(),
                    transactionAccount.getPayload().get(PartnerAccountPayload.IGNORE_BALANCE));

            final List<Transaction> transactionsForAccount = transactionAccount.getTransactions().stream()
                    .map(t -> {
                        Transaction transaction = SEBUtils.createTransaction(
                                user, credentials, account, t,
                                serviceConfiguration.getTransaction().isEnforceRemovalOfPendingAfterExpired());

                        if (!Objects.equals(container.getType(), TransactionContainerType.HISTORICAL)) {
                            log.debug(credentials, String.format("Updating transaction: %s",
                                    MoreObjects.toStringHelper(transaction)
                                            .add("id", transaction.getId())
                                            .add("externalId", t.getExternalId())
                                            .add("accountId", transaction.getAccountId())
                                            .add("externalAccountId", externalAccountId)
                                            .add("date", transaction.getDate())
                                            .toString()));
                        }

                        return transaction;
                    }).collect(Collectors.toList());

            log.info(credentials, String.format("Received %d transactions for [accountId:%s, externalAccountId:%s].",
                    transactionsForAccount.size(), account.getId(), externalAccountId));

            List<String> allExternalIdsForAccount = transactionsForAccount.stream()
                    .map(t -> t.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID)).collect(Collectors.toList());
            Set<String> uniqueExternalIdsForAccount = Sets.newHashSet(allExternalIdsForAccount);

            if (allExternalIdsForAccount.size() != uniqueExternalIdsForAccount.size()) {
                log.info(user.getId(), credentials.getId(),
                        String.format("Not all transaction externalId:s were unique for account with externalId = %s.",
                                externalAccountId));
                HttpResponseHelper.error(Status.BAD_REQUEST);
            }

            transactions.addAll(transactionsForAccount);
        }

        reportTransactionMetric(transactions, container.getType());

        // Update our systems.

        credentials.setStatus(CredentialsStatus.UPDATING);
        updateCredentials(credentials);

        if (!Objects.equals(container.getType(), TransactionContainerType.HISTORICAL)) {
            // Don't update account on HISTORICAL containerTypes (initial load & resyncs)
            // In both these cases the partner should do ingest accounts, with the latest info,
            // prior to an ingest transactions with HISTORICAL containerType.
            for (TransactionAccountEntity transactionAccount : container.getTransactionAccounts()) {
                updateAccount(accountsByExternalId.get(transactionAccount.getExternalId()));
            }
        }

        if (!transactions.isEmpty()) {
            updateTransactions(user, credentials, transactions, container.getType());
        }
    }

    private void setBalance(Account account, Double disposableAmount, Double balance, Object ignoreBalance) {
        if (disposableAmount != null) {
            account.setBalance(disposableAmount);
        } else if (!Objects.equals(ignoreBalance, true)) {
            account.setBalance(balance);
        }
    }

    @Override
    public void deleteTransactions(String externalUserId, DeleteTransactionAccountContainer container) {
        if (!container.isValid(externalUserId)) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        final User user = validateUser(externalUserId);
        final Credentials credentials = validateCredentials(user);
        final ImmutableMap<String, Account> accountsByExternalId = Maps.uniqueIndex(getAccounts(user, credentials),
                Account::getBankId);

        List<TransactionToDelete> transactionsToDelete = Lists.newArrayList();

        for (DeleteTransactionAccountEntity transactionAccount : container.getTransactionAccounts()) {
            final String externalAccountId = transactionAccount.getExternalId();
            final Account account = accountsByExternalId.get(externalAccountId);

            if (account == null) {
                log.info(user.getId(), credentials.getId(),
                        String.format("No account found for externalAccountId:%s.", externalAccountId));
                HttpResponseHelper.error(Status.BAD_REQUEST);
            }

            setBalance(account, transactionAccount.getDisposableAmount(), transactionAccount.getBalance(),
                    transactionAccount.getPayload().get(PartnerAccountPayload.IGNORE_BALANCE));

            updateAccount(account);

            transactionAccount.getTransactions()
                    .forEach(dte -> {
                        String externalId = dte.getExternalId();
                        transactionsToDelete.add(TransactionToDelete.create(externalId, account.getId()));
                        log.info(user.getId(),
                                String.format("Submitting delete transaction for externalId: %s.", externalId));
                    });
        }

        DeleteTransactionRequest request = new DeleteTransactionRequest();
        request.setUserId(user.getId());
        request.setTransactions(transactionsToDelete);

        DeleteTransactionTask task = new DeleteTransactionTask();
        task.setPayload(request);
        task.setPartitionKey(user.getId());

        try {
            Uninterruptibles.getUninterruptibly(taskSubmitter.submit(task));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Timed
    public void user(UserEntity userEntity) {
        if (!userEntity.isValid()) {
            log.info("Invalid userEntity");
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        // Validate if this user already exists.

        User existingUser = userRepository.findOneByUsername(userEntity.getExternalId());

        if (existingUser != null) {
            log.info("User already exists");
            HttpResponseHelper.error(Status.CONFLICT);
        }

        User user = SEBUtils.createUser(userEntity, getMarket(Market.Code.SE.name()), flagsConfiguration);
        userRepository.save(user);
        userStateRepository.save(new UserState(user.getId()));

        log.info(user.getId(), "Creating user with token " + userEntity.getExternalId());
    }

    private void updateTransactions(User user, Credentials credentials, List<Transaction> transactions,
            TransactionContainerType type) {

        final TasksQueueConfiguration.Modes taskQueueMode = (serviceConfiguration.getTaskQueue()
                != null)
                ? serviceConfiguration.getTaskQueue().getMode()
                : TasksQueueConfiguration.DEFAULT_MODE;

        // Update transactions the old way, if the task queue is not enabled (i.e. disabled or in test mode).
        if (!TasksQueueConfiguration.SHOULD_CONSUME.contains(taskQueueMode)) {

            UpdateTransactionsRequest request = new UpdateTransactionsRequest();

            request.setUser(user.getId());
            request.setCredentials(credentials.getId());
            request.setUserTriggered(Objects.equals(TransactionContainerType.HISTORICAL, type));
            request.setTransactions(CoreTransactionMapper.toSystemTransaction(transactions));

            systemServiceFactory.getProcessService().updateTransactionsAsynchronously(request);
        }

        // Send the task to the queue if the task queue is enabled or in test mode.
        if (TasksQueueConfiguration.SHOULD_PRODUCE.contains(taskQueueMode)) {
            boolean userTriggered = Objects.equals(TransactionContainerType.HISTORICAL, type);
            boolean realTime = (type == TransactionContainerType.REAL_TIME);
            metricRegistry.meter(TRANSACTION_TOPIC_METRIC.label("type", type.name())).inc();

            try {
                transactionIngestionHandler
                        .ingestTransactions(user.getId(), credentials, userTriggered, realTime, transactions);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<Account> getAccounts(User user, Credentials credentials) {
        return accountRepository.findByUserIdAndCredentialsId(user.getId(), credentials.getId());
    }

    private Market getMarket(final String code) {
        return Iterables.find(marketSupplier.get(), m -> Objects.equals(code, m.getCodeAsString()), null);
    }

    private void logNotification(Notification notification) {
        try {
            log.info(String.format("Received notification: %s", mapper.writeValueAsString(notification)));
        } catch (JsonProcessingException e) {
            log.info("Could not log notification.");
        }
    }

    private Credentials validateCredentials(User user) {
        List<Credentials> credentialsList = credentialsRepository.findAllByUserIdAndProviderName(user.getId(),
                SEBUtils.SEB_PROVIDER_NAME);

        if (credentialsList.size() > 1) {
            log.error(user.getId(), "Invalid state: more than one credentials exist for the user.");
            HttpResponseHelper.error(Status.INTERNAL_SERVER_ERROR);
        }

        if (credentialsList.size() == 1) {
            return credentialsList.get(0);
        }

        Credentials credentials = SEBUtils.createCredentials(user);
        credentialsRepository.save(credentials);

        return credentials;
    }

    private User validateUser(String externalUserId) {
        if (Strings.isNullOrEmpty(externalUserId)) {
            log.info("No user id supplied.");
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        User user = userRepository.findOneByUsername(externalUserId);

        if (user == null) {
            log.info(String.format("No user matching SEB user id %s.", externalUserId));
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    @Timed
    public void deleteUser(String externalId) {
        User user = validateUser(externalId);

        log.info(user.getId(), "Deleting user.");

        DeleteUserRequest deleteUserRequest = new DeleteUserRequest();

        deleteUserRequest.setUserId(user.getId());

        systemServiceFactory.getUpdateService().deleteUser(deleteUserRequest);
    }

    @Override
    public String ping() {
        // Note that SEB load balancers asserts that response body is this exact string.
        return "pong";
    }

    @Override
    @Timed
    public void rollback(final RollbackRequest rollbackRequest) {
        if (rollbackRequest.getCheckpointIds() == null || rollbackRequest.getCheckpointIds().size() == 0) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        for (final String checkpointId : rollbackRequest.getCheckpointIds()) {
            log.info(String.format("Deleting checkpoint %s.", checkpointId));

            final Checkpoint checkpoint = new Checkpoint();
            checkpoint.setCheckpointId(checkpointId);
            checkpoint.setDate(new Date());
            this.checkpointRepository.save(checkpoint);

            userRepository.streamAll().forEach(user -> {
                CheckpointRollbackRequest request = new CheckpointRollbackRequest();
                request.setCheckpointId(checkpoint.getCheckpointId());
                request.setUserId(user.getId());

                CheckpointRollbackTask task = new CheckpointRollbackTask();
                task.setPayload(request);
                task.setPartitionKey(user.getId());

                log.info(user.getId(),
                        String.format("TASK QUEUE: Submit rollback for checkpointId: %s.",
                                request.getCheckpointId()));

                try {
                    Uninterruptibles.getUninterruptibly(taskSubmitter.submit(task));
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
