package se.tink.backend.connector.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.modelmapper.ModelMapper;
import se.tink.backend.abnamro.utils.AbnAmroCredentialsUtils;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.mapper.CoreTransactionMapper;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedAccountRepository;
import se.tink.backend.common.repository.mysql.main.AbnAmroBufferedTransactionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.connector.abnamro.TransactionBufferContext;
import se.tink.backend.connector.abnamro.exceptions.AccountNotFoundException;
import se.tink.backend.connector.abnamro.exceptions.CredentialsNotFoundException;
import se.tink.backend.connector.abnamro.exceptions.DuplicateCredentialsException;
import se.tink.backend.connector.api.AbnAmroConnectorService;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.rpc.abnamro.TransactionAccountContainer;
import se.tink.backend.connector.rpc.abnamro.TransactionAccountEntity;
import se.tink.backend.connector.rpc.abnamro.TransactionEntity;
import se.tink.backend.connector.util.handler.TransactionIngestionHandler;
import se.tink.backend.core.AbnAmroBufferedAccount;
import se.tink.backend.core.AbnAmroBufferedTransaction;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AbnAmroConnectorServiceResource extends ConnectorServiceResource implements AbnAmroConnectorService {

    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("buffered-transactions-executor-%d")
            .build();
    private final ServiceConfiguration serviceConfiguration;
    private final TransactionIngestionHandler transactionIngestionHandler;

    private static class PageStatus {
        // Historical transactions. Page with 50 transactions. More pages pending.
        static final int HISTORY_PENDING = 2;
        // Historical transactions. Page with up to 50 transactions. No more pages to come.
        static final int HISTORY_LAST = 1;
        // New transaction.
        static final int SINGLE = 0;

        public static TransactionContainerType getTransactionContainerType(int status) {
            switch (status) {
            case SINGLE:
                return TransactionContainerType.REAL_TIME;
            case HISTORY_LAST:
            case HISTORY_PENDING:
                return TransactionContainerType.HISTORICAL;
            }
            return null;
        }
    }

    private static final int EXECUTOR_SHUTDOWN_SLACK_SECONDS = 10;

    private static final String BUFFER_LOCK_PREFIX = "/locks/connector/buffer/credentials/";
    private static final int BUFFER_TIMEOUT = 1;
    private static final TimeUnit BUFFER_TIMEOUT_UNIT = TimeUnit.MINUTES;

    private static final String AUTO_RELEASE_LOCK_PREFIX = "/locks/connector/buffer/autorelease/credentials/";
    private static final long AUTO_RELEASE_TIMEOUT = 40; // Seconds.
    private static final TimeUnit AUTO_RELEASE_TIMEOUT_UNIT = TimeUnit.SECONDS;

    private static final int ABNORMAL_TRANSACTION_COUNT = 25000;

    private static final ModelMapper MAPPER = new ModelMapper();

    private final CuratorFramework coordinationClient;

    private final AbnAmroBufferedAccountRepository abnAmroBufferedAccountRepository;
    private final AbnAmroBufferedTransactionRepository abnAmroBufferedTransactionRepository;
    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;
    private ListenableThreadPoolExecutor<Runnable> bufferedTransactionsExecutor;

    private static final double PERMITS_PER_SECOND = 1000;
    private static final int DEFAULT_PERMITS = 50; // 1000 / 50 = 20 requests per second
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(PERMITS_PER_SECOND);
    private final ImmutableMap<Integer, Integer> rateLimitPermits;

    private LogUtils log;

    private static final int MAX_TRANSACTION_COUNT = 10000;
    private static final MetricId TRANSACTION_TOPIC_METRIC = MetricId.newId("tink_transaction_topic_leeds");

    @Inject
    public AbnAmroConnectorServiceResource(MetricRegistry metricRegistry,
            SystemServiceFactory systemServiceFactory, TaskSubmitter taskSubmitter,
            CuratorFramework zookeeperClient, AbnAmroBufferedAccountRepository abnAmroBufferedAccountRepository,
            AbnAmroBufferedTransactionRepository abnAmroBufferedTransactionRepository,
            AccountRepository accountRepository, CredentialsRepository credentialsRepository,
            UserRepository userRepository, ServiceConfiguration serviceConfiguration) {
        super(metricRegistry, systemServiceFactory, taskSubmitter);
        this.log = new LogUtils(this.getClass());

        this.coordinationClient = zookeeperClient;

        this.abnAmroBufferedAccountRepository = abnAmroBufferedAccountRepository;
        this.abnAmroBufferedTransactionRepository = abnAmroBufferedTransactionRepository;
        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;

        this.serviceConfiguration = serviceConfiguration;

        this.rateLimitPermits = serviceConfiguration.getAbnAmro().getRateLimitPermits();
        this.transactionIngestionHandler = new TransactionIngestionHandler(serviceConfiguration.getTaskQueue(),
                taskSubmitter);
    }

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    @Timed
    public void transactions(TransactionAccountContainer container) {

        double timeSlept = RATE_LIMITER.acquire(getPermits(container));
        log.debug(String.format("Slept for %.0fms.", timeSlept * 1000));

        final TransactionAccountEntity accountWithTransactions = container.getAccount();

        Runnable runnable = () -> processAccountWithTransactions(accountWithTransactions);

        boolean sync = (accountWithTransactions.getStatus() == PageStatus.SINGLE);

        // Process transactions synchronously if they are single (real time) transactions or process them on the thread
        // pool if it is historical data. We would like to process all transactions synchronously and only return "OK"
        // back to ABN AMRO if the processing was successful. It is unfortunately hard to achieve with the locks and
        // auto-release of buffers that we currently have in place. ABN ARMO will retry sending the transactions to us
        // in case of failure at our side
        if (sync) {
            runnable.run();
        } else {
            bufferedTransactionsExecutor.execute(runnable);
        }
    }

    private void appendBuffer(TransactionBufferContext context) {

        final Credentials credentials = context.getCredentials();
        final TransactionAccountEntity accountWithTransactions = context.getAccountWithTransactions();

        if (!accountWithTransactions.getTransactions().isEmpty()) {

            List<AbnAmroBufferedTransaction> abnAmroTransactions = Lists.newArrayList(Iterables.transform(
                    accountWithTransactions.getTransactions(),
                    input -> {
                        AbnAmroBufferedTransaction transaction = MAPPER
                                .map(input, AbnAmroBufferedTransaction.class);
                        transaction.setAccountNumber(accountWithTransactions.getAccountNumber());
                        transaction.setCredentialsId(credentials.getId());
                        return transaction;
                    }));

            abnAmroBufferedTransactionRepository.save(abnAmroTransactions);
        }
    }

    private void updateBufferStatus(TransactionBufferContext context) {
        final Credentials credentials = context.getCredentials();
        final TransactionAccountEntity transactionAccount = context.getAccountWithTransactions();

        // Fetch all buffered accounts.
        List<AbnAmroBufferedAccount> bufferedAccounts = abnAmroBufferedAccountRepository
                .findByCredentialsId(credentials.getId());

        AbnAmroBufferedAccount bufferedAccount = Iterables.find(bufferedAccounts,
                account -> (account.getAccountNumber() == transactionAccount.getAccountNumber()), null);

        // Update or create the buffered account currently being handled.

        if (bufferedAccount == null) {
            bufferedAccount = new AbnAmroBufferedAccount();
            bufferedAccount.setAccountNumber(transactionAccount.getAccountNumber());
            bufferedAccount.setCredentialsId(credentials.getId());

            // Important to add the new account to the list of buffered accounts so that we can check if
            // we completed all accounts
            bufferedAccounts.add(bufferedAccount);
        }

        if (!bufferedAccount.isComplete()) {

            boolean complete = transactionAccount.getStatus() == PageStatus.HISTORY_LAST;

            log.debug(credentials,
                    String.format("Updated buffered account complete status (Account = '%d', Complete = '%s')",
                            bufferedAccount.getAccountNumber(), complete));

            bufferedAccount.setComplete(complete);
        }

        bufferedAccount.setTransactionCount(bufferedAccount.getTransactionCount()
                + transactionAccount.getTransactions().size());

        abnAmroBufferedAccountRepository.save(bufferedAccount);

        // Update the buffer status on the context.

        // The number of buffered transactions per account.
        context.setBufferedCountByAccountNumber(Maps.transformValues(
                Maps.uniqueIndex(bufferedAccounts, AbnAmroUtils.Functions.BUFFERED_ACCOUNT_TO_ACCOUNT_NUMBER),
                AbnAmroUtils.Functions.BUFFERED_ACCOUNT_TO_TRANSACTION_COUNT));

        // The account numbers of the complete accounts (i.e. where the last page has been received).
        context.setCompleteAccounts(Sets.newHashSet(Iterables.transform(
                Iterables.filter(bufferedAccounts, AbnAmroUtils.Predicates.COMPLETE_BUFFERED_ACCOUNTS),
                AbnAmroUtils.Functions.BUFFERED_ACCOUNT_TO_ACCOUNT_NUMBER)));
    }

    private void autoReleaseBufferIfNecessary(TransactionBufferContext context, DistributedBarrier barrier) {

        final Credentials credentials = context.getCredentials();

        removeAutoReleaseBarrier(barrier, credentials);

        // The buffer has already been released; no need to set up an auto-release.
        if (!context.getTransactions().isEmpty()) {
            return;
        }

        try {
            // Create a barrier for auto-releasing the buffer.
            barrier.setBarrier();

            // Auto-release the buffer if
            if (!barrier.waitOnBarrier(AUTO_RELEASE_TIMEOUT, AUTO_RELEASE_TIMEOUT_UNIT)) {
                barrier.removeBarrier();
                log.info(credentials.getUserId(), credentials.getId(), "Auto-releasing buffer.");
                releaseBuffer(context);
            }
        } catch (Exception e) {
            log.error(credentials.getUserId(), credentials.getId(), "Unable to acquire auto-release lock.", e);
        }
    }

    private boolean blocked(Credentials credentials, boolean refreshBeforeChecking) {
        if (refreshBeforeChecking) {
            refreshCredentialPropertiesFromDatabase(credentials);
        }

        return AbnAmroUtils.Predicates.IS_BLOCKED.apply(credentials);
    }

    private boolean blockIfAbnormalTransactionCount(TransactionBufferContext context) {

        final Credentials credentials = context.getCredentials();
        final long transactionCount = context.getBufferedCount();

        if (transactionCount > ABNORMAL_TRANSACTION_COUNT) {

            log.info(credentials.getUserId(), credentials.getId(),
                    "Blocked credentials due to abnormal transactions count.");

            Catalog catalog = getCatalog(credentials.getUserId());

            // Block the credentials.
            credentials.setPayload(AbnAmroUtils.CREDENTIALS_BLOCKED_PAYLOAD);
            credentials.setStatus(CredentialsStatus.PERMANENT_ERROR);
            credentials.setStatusPayload(catalog
                    .getString("The bank connection was blocked due to an abnormal number of transactions."));
            credentials.setStatusUpdated(new Date());

            updateCredentials(credentials);

            // Delete the buffered transactions.
            abnAmroBufferedTransactionRepository.deleteByCredentialsId(credentials.getId());

            return true;
        } else {
            return false;
        }
    }

    private void buffer(TransactionBufferContext context) {

        final Credentials credentials = context.getCredentials();

        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(coordinationClient, BUFFER_LOCK_PREFIX
                + credentials.getId());

        try {
            if (!lock.acquire(BUFFER_TIMEOUT, BUFFER_TIMEOUT_UNIT)) {
                throw new RuntimeException(String.format(
                        "[%s] Could not acquire lock when buffering transactions.", credentials.getId()));
            }

            // Since the credentials might have been updated since it was fetched, refresh it before checking if it has
            // been blocked. Don't proceed with the buffering if the credentials are blocked.
            if (blocked(credentials, true)) {
                return;
            }

            appendBuffer(context);
            updateBufferStatus(context);

            // Block the credentials if an abnormal amount of transactions have been buffered.
            if (blockIfAbnormalTransactionCount(context)) {
                return;
            }

            // Release the buffer if it's complete.
            if (context.isBufferComplete()) {
                log.trace(credentials, "Releasing buffer.");
                releaseBuffer(context);
            } else {
                log.trace(credentials, "Not releasing buffer.");
            }

        } catch (Exception e) {
            log.error(credentials.getUserId(), credentials.getId(), "Unable to acquire lock for buffering.", e);
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                try {
                    lock.release();
                } catch (Exception e) {
                    log.error(credentials.getUserId(), credentials.getId(), "Unable to release lock for buffering.", e);
                }
            } else {
                log.warn(credentials.getUserId(), credentials.getId(), "Buffer lock is not acquired in this process.");
            }
        }
    }

    private void bufferIfNecessary(TransactionBufferContext context) {

        final Credentials credentials = context.getCredentials();
        final TransactionAccountEntity accountWithTransactions = context.getAccountWithTransactions();

        // Buffer transactions for credentials that are awaiting historical data. This includes single transactions
        // that might arrive during the time historical data is loaded. Pages for credentials that aren't awaiting
        // historical transactions are ignored.
        if (credentials.getStatus() == CredentialsStatus.UPDATING) {

            DistributedBarrier barrier = new DistributedBarrier(coordinationClient, AUTO_RELEASE_LOCK_PREFIX
                    + credentials.getId());

            removeAutoReleaseBarrier(barrier, credentials);
            buffer(context);
            autoReleaseBufferIfNecessary(context, barrier);
        }
    }

    private Catalog getCatalog(String userId) {
        User user = userRepository.findOne(userId);
        String locale;

        if (user != null) {
            locale = user.getProfile().getLocale();
        } else {
            locale = "nl_NL";
            log.warn(userId, String.format("Unable to fetch locale for user. Using fallback (%s).", locale));
        }

        return Catalog.getCatalog(locale);
    }

    private int getPermits(TransactionAccountContainer container) {
        return rateLimitPermits.getOrDefault(container.getAccount().getStatus(), DEFAULT_PERMITS);
    }

    private static Transaction getTransaction(Account account, TransactionEntity transactionEntity) {

        Transaction transaction = getTransaction(transactionEntity);
        transaction.setAccountId(account.getId());
        transaction.setCredentialsId(account.getCredentialsId());
        transaction.setUserId(account.getUserId());
        return transaction;
    }

    private static Transaction getTransaction(TransactionEntity transactionEntity) {

        final Map<String, String> descriptionParts = AbnAmroUtils
                .getDescriptionParts(transactionEntity.getDescription());

        Transaction transaction = new Transaction();

        transaction.setAmount(transactionEntity.getAmount());
        transaction.setDate(transactionEntity.getDate());
        transaction.setDescription(AbnAmroUtils.getDescription(descriptionParts));
        transaction.setType(AbnAmroUtils.getTransactionType(transactionEntity.getType()));

        // Set the counterpart account number.
        final String cpAccount = StringUtils.trimToNull(transactionEntity.getCpAccount());
        if (cpAccount != null) {
            transaction.setPayload(TransactionPayloadTypes.TRANSFER_ACCOUNT_EXTERNAL, cpAccount);
        }

        // Set the counterpart account name.
        final String cpName = StringUtils.trimToNull(transactionEntity.getCpName());
        if (cpName != null) {
            transaction.setPayload(TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL, cpName);
        }

        // Set the transaction message.
        final String message = StringUtils.trimToNull(descriptionParts.get(AbnAmroUtils.DescriptionKeys.DESCRIPTION));
        if (message != null && !Objects.equal(message, transaction.getDescription())) {
            transaction.setPayload(TransactionPayloadTypes.MESSAGE, message);
        }

        // Set the ABN AMRO payload.
        if (transactionEntity.getPayload() != null && !transactionEntity.getPayload().isEmpty()) {

            Map<String, Object> payload = transactionEntity.getPayload();

            // Extract the external transaction id from the payload and persist it separately in the internal payload.
            if (payload.containsKey(AbnAmroUtils.ExternalPayloadKeys.TRANSACTION_ID)) {
                String externalId = payload.get(AbnAmroUtils.ExternalPayloadKeys.TRANSACTION_ID).toString();
                transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, externalId);
                payload.remove(AbnAmroUtils.ExternalPayloadKeys.TRANSACTION_ID);
            }

            // If the payload only consisted of the external transaction id, it's now empty and there's no point of
            // persisting it.
            if (!payload.isEmpty()) {
                String serializedPayload = SerializationUtils.serializeToString(payload);
                transaction.setInternalPayload(AbnAmroUtils.InternalPayloadKeys.ABNAMRO_PAYLOAD, serializedPayload);
            }
        }

        // Set the original description lines.
        if (transactionEntity.getDescription() != null && !transactionEntity.getDescription().isEmpty()) {
            String serializedDescriptionLines = SerializationUtils
                    .serializeToString(transactionEntity.getDescription());
            transaction
                    .setInternalPayload(AbnAmroUtils.InternalPayloadKeys.DESCRIPTION_LINES, serializedDescriptionLines);
        }

        // Keep track of when the transaction was received in the connector to be able to track the full processing
        // chain from when we received it in the connector until activities are generated
        transaction.setInternalPayload(Transaction.InternalPayloadKeys.INCOMING_TIMESTAMP,
                String.valueOf(transactionEntity.getEntityCreated().getTime()));

        return transaction;
    }

    private void updateAccountBalance(Account account, double balance) {
        log.info(account.getUserId(),
                String.format("Updating account balance (AccountId: '%s', BankId = '%s', Old = '%f', New = '%f')",
                        account.getId(), account.getBankId(), account.getBalance(), balance));

        account.setBalance(balance);
        updateAccount(account);
    }

    private void processAccountWithTransactions(final TransactionAccountEntity accountWithTransactions) {
        log.info(String.format("Received (%s)", accountWithTransactions));

        accountWithTransactions.getBcNumbers().forEach(bcNumber -> {
            // It is important that the processing is done in a try-catch block since we get exceptions for one
            // user but we want to continue processing on others.
            try {
                Credentials credentials = getAbnAmroCredential(bcNumber);

                if (accountWithTransactions.getStatus() == PageStatus.SINGLE) {
                    processSingleTransaction(credentials, accountWithTransactions);
                } else {
                    processHistoricalTransactions(credentials, accountWithTransactions);
                }

            } catch (DuplicateCredentialsException | CredentialsNotFoundException | AccountNotFoundException e) {
                log.warn(e.getMessage());
            }
        });
    }

    private void processHistoricalTransactions(Credentials credentials, TransactionAccountEntity entity) {

        if (!AbnAmroCredentialsUtils.isEligibleForHistoryTransactions(credentials)) {
            log.warn(credentials, "Credential is not qualified for historical transactions.");
            return;
        }

        TransactionBufferContext context = getTransactionBufferContext(credentials, entity);
        processTransactions(context);
        processCredentials(context);
    }

    private void processSingleTransaction(Credentials credentials, TransactionAccountEntity entity)
            throws AccountNotFoundException {

        if (!AbnAmroCredentialsUtils.isEligibleForSingleTransactions(credentials)) {
            // Would be nice to be able to return 404 bad request here but it is hard since one transaction can
            // belong to multiple users.
            log.warn(credentials, "Credential is not qualified for new transactions.");
            return;
        }

        final String bankId = AbnAmroUtils.getBankId(entity.getAccountNumber());

        Account account = getAccountByCredentialAndBankId(credentials, bankId);

        if (Objects.equal(account.getPayload(AbnAmroUtils.InternalAccountPayloadKeys.LOCKED), "locked")) {
            log.info(account, "Account locked");
            // TODO Return here when dropping CJ split transactions
            // return;
        }

        // 1. Update the account balance
        updateAccountBalance(account, entity.getBalance());

        // 2. Process the new transaction
        processTransactions(credentials, account, entity);
    }

    /**
     * Get the ABN AMRO credentials.
     * <p>
     * The logic is different depending on if it is an old user (Grip < 4.0) or a new user (Grip => 4.0). BcNumber is
     * stored on the username field for Grip < 4.0 users and on the payload field on the credentials for Grip => 4.0.
     *
     * @throws CredentialsNotFoundException if an ABN AMRO credential isn't found
     */
    private Credentials getAbnAmroCredential(Long bcNumber) throws CredentialsNotFoundException,
            DuplicateCredentialsException {
        // Try to find a user by username. The bcNumber is stored on the username for all Grip < 4.0 users.
        User user = userRepository.findOneByUsername(AbnAmroLegacyUserUtils.getUsername(bcNumber.toString()));

        List<Credentials> credentials;

        if (user == null) {
            // We didn't find a user. This means that the user doesn't exist or that it is a Grip => 4.0 user.
            credentials = credentialsRepository.findAllByPayload(bcNumber.toString());
        } else {
            // We found a user, pick up all the ABN AMRO credentials.
            credentials = credentialsRepository
                    .findAllByUserIdAndProviderName(user.getId(), AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);
        }

        if (credentials == null || credentials.isEmpty()) {
            throw new CredentialsNotFoundException(bcNumber);
        }

        if (credentials.size() > 1) {
            throw new DuplicateCredentialsException(bcNumber);
        }

        return credentials.get(0);
    }

    /**
     * Get an account by credential and bankId
     *
     * @throws AccountNotFoundException if the account isn't found
     */
    private Account getAccountByCredentialAndBankId(Credentials credentials, String bankId)
            throws AccountNotFoundException {
        Account account = accountRepository
                .findByUserIdAndCredentialsIdAndBankId(credentials.getUserId(), credentials.getId(), bankId);

        if (account == null) {
            throw new AccountNotFoundException(credentials, bankId);
        }

        return account;
    }

    private TransactionBufferContext getTransactionBufferContext(Credentials credentials,
            TransactionAccountEntity accountWithTransactions) {

        ImmutableSet<Long> accountNumbers = FluentIterable
                .from(abnAmroBufferedAccountRepository.findByCredentialsId(credentials.getId()))
                .transform(AbnAmroUtils.Functions.BUFFERED_ACCOUNT_TO_ACCOUNT_NUMBER)
                .toSet();

        TransactionBufferContext context = new TransactionBufferContext();
        context.setAccountsNumbers(accountNumbers);
        context.setAccountWithTransactions(accountWithTransactions);
        context.setCredentials(credentials);

        return context;
    }

    private void preprocessTransactions(List<TransactionEntity> transactionEntities) {
        for (TransactionEntity transactionEntity : transactionEntities) {

            Map<String, Object> payload = transactionEntity.getPayload();
            String originType = transactionEntity.getOriginType();

            if (payload == null) {
                payload = Maps.newHashMap();
                transactionEntity.setPayload(payload);
            }

            // We get an id for the service which originated the transaction which can be used to determine the
            // prioritization of the transaction when sending it to system
            if (!Strings.isNullOrEmpty(originType)) {
                payload.put(AbnAmroUtils.ExternalPayloadKeys.ORIGIN_TYPE, originType);
            }

            // The transaction timestamp has millisecond resolution (well, it's actually only 1/100th of a second) and
            // is unique within the scope of the account. Hence, it can be used as an identifier. The millisecond
            // resolution is lost when persisted in MySQL (5.5) when buffered, and the date might get flattened or
            // modified in another way later. To not lose the property of being an identifier, explicitly store it as an
            // external id in the payload.
            if (!payload.containsKey(AbnAmroUtils.ExternalPayloadKeys.TRANSACTION_ID)) {
                String transactionId = Long.toString(transactionEntity.getDate().getTime());
                payload.put(AbnAmroUtils.ExternalPayloadKeys.TRANSACTION_ID, transactionId);
            }
        }
    }

    private void processCredentials(TransactionBufferContext context) {

        final Credentials credentials = context.getCredentials();

        // Don't process blocked credentials.
        if (blocked(credentials, true)) {
            return;
        }

        int numberOfBufferedTransactions = context.getBufferedCountForEnabledAccounts();

        if (numberOfBufferedTransactions > 0) {
            Catalog catalog = getCatalog(credentials.getUserId());
            String format = catalog.getPluralString("Loaded one transaction", "Loaded {0} transactions",
                    numberOfBufferedTransactions);
            credentials.setStatusPayload(Catalog.format(format, numberOfBufferedTransactions));
        } else {
            credentials.setStatusPayload(null);
        }

        updateCredentials(credentials);
    }

    private void processTransactions(TransactionBufferContext context) {

        preprocessTransactions(context.getAccountWithTransactions().getTransactions());
        bufferIfNecessary(context);

        // Don't process if transactions are empty (due to them being buffered and waiting for more transactions).
        if (context.getTransactions().isEmpty()) {
            return;
        }

        final Credentials credentials = context.getCredentials();

        List<Transaction> transactions = Lists.newArrayList();
        List<String> origins = Lists.newArrayList();

        ImmutableMap<String, Account> accountsByBankId = FluentIterable
                .from(accountRepository.findByCredentialsId(credentials.getId()))
                .uniqueIndex(Account::getBankId);

        for (long accountNumber : context.getTransactions().keySet()) {

            String bankId = AbnAmroUtils.getBankId(accountNumber);
            Account account = accountsByBankId.get(bankId);

            if (account == null) {
                log.warn(credentials.getUserId(), credentials.getId(),
                        String.format("No account with bank id '%s' exists.", bankId));
                continue;
            }

            List<TransactionEntity> rawTransactions = context.getTransactions().get(accountNumber);

            for (TransactionEntity rawTransaction : rawTransactions) {
                origins.add(rawTransaction.getOrigin());
                transactions.add(getTransaction(account, rawTransaction));
            }
        }

        int pageStatus = context.getAccountWithTransactions().getStatus();
        reportTransactionMetric(transactions, PageStatus.getTransactionContainerType(pageStatus));

        updateTransactions(credentials, transactions, origins);
    }

    private void processTransactions(Credentials credentials, Account account,
            TransactionAccountEntity accountWithTransactions) {

        preprocessTransactions(accountWithTransactions.getTransactions());

        List<Transaction> transactions = accountWithTransactions.getTransactions()
                .stream()
                .map(t -> getTransaction(account, t))
                .collect(Collectors.toList());

        int pageStatus = accountWithTransactions.getStatus();
        reportTransactionMetric(transactions, PageStatus.getTransactionContainerType(pageStatus));

        List<String> origins = accountWithTransactions.getTransactions()
                .stream()
                .map(TransactionEntity::getOrigin)
                .collect(Collectors.toList());

        updateTransactions(credentials, transactions, origins);
    }

    private void refreshCredentialPropertiesFromDatabase(final Credentials credentials) {
        Credentials freshCredentials = credentialsRepository.findOne(credentials.getId());
        credentials.setPayload(freshCredentials.getPayload());
        credentials.setStatus(freshCredentials.getStatus());
        credentials.setStatusPayload(freshCredentials.getStatusPayload());
        credentials.setStatusUpdated(freshCredentials.getStatusUpdated());
    }

    private void releaseBuffer(TransactionBufferContext context) {

        final String credentialsId = context.getCredentials().getId();

        for (AbnAmroBufferedTransaction t : abnAmroBufferedTransactionRepository.findByCredentialsId(credentialsId)) {
            context.addTransaction(t.getAccountNumber(), MAPPER.map(t, TransactionEntity.class));
        }

        abnAmroBufferedAccountRepository.deleteByCredentialsId(credentialsId);
        abnAmroBufferedTransactionRepository.deleteByCredentialsId(credentialsId);
    }

    private void removeAutoReleaseBarrier(DistributedBarrier barrier, Credentials credentials) {
        try {
            // Remove existing auto-release barrier.
            barrier.removeBarrier();
        } catch (Exception e) {
            log.error(credentials.getUserId(), credentials.getId(), "Unable to remove auto-release lock.", e);
        }
    }

    private void updateTransactions(Credentials credentials, List<Transaction> transactions, List<String> origins) {

        UpdateTransactionsRequest request = new UpdateTransactionsRequest();

        request.setTransactions(CoreTransactionMapper.toSystemTransaction(transactions));
        request.setUser(credentials.getUserId());
        request.setCredentials(credentials.getId());
        request.setUserTriggered(transactions.size() > 1);

        if (transactions.size() == 1) {
            // For single transactions, submit the request to the Kafka queue if enabled (directly to system otherwise).
            final TasksQueueConfiguration.Modes taskQueueMode = (serviceConfiguration.getTaskQueue()
                    != null)
                    ? serviceConfiguration.getTaskQueue().getMode()
                    : TasksQueueConfiguration.DEFAULT_MODE;

            // Update transactions the old way, if the task queue is not enabled (i.e. disabled or in test mode).
            if (!TasksQueueConfiguration.SHOULD_CONSUME.contains(taskQueueMode)) {
                systemServiceFactory.getProcessService().updateTransactionsAsynchronously(request);
            }

            // Send the task to the queue if the task queue is enabled or in test mode.
            if (TasksQueueConfiguration.SHOULD_PRODUCE.contains(taskQueueMode)) {
                if (transactions.size() > MAX_TRANSACTION_COUNT) {
                    log.warn(String.format("Too many transactions (%s). Taking the %s most current.",
                            transactions.size(),
                            MAX_TRANSACTION_COUNT));

                    List<Transaction> coreTransactions = transactions.stream()
                            .sorted(Orderings.TRANSACTION_DATE_ORDERING.reversed())
                            .limit(MAX_TRANSACTION_COUNT).collect(Collectors.toList());
                    request.setTransactions(CoreTransactionMapper.toSystemTransaction(coreTransactions));
                }

                boolean realTime = false;
                if (origins.size() > 0 && origins.get(0) != null) {
                    realTime = origins.get(0).equals(TransactionEntity.USER_INITIATED_ORIGIN);
                    metricRegistry.meter(TRANSACTION_TOPIC_METRIC.label("origin", origins.get(0))).inc();
                } else {
                    metricRegistry.meter(TRANSACTION_TOPIC_METRIC.label("origin", "no_origin")).inc();
                }

                try {
                    transactionIngestionHandler
                            .ingestTransactions(credentials.getUserId(), credentials, false, realTime, transactions);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            // Send batches of historical data directly to system, "the old way".
            systemServiceFactory.getProcessService().updateTransactionsAsynchronously(request);
        }
    }

    @PostConstruct
    public void start() throws Exception {
        bufferedTransactionsExecutor = ListenableThreadPoolExecutor.builder(
                new LinkedBlockingQueue<>(),
                new TypedThreadPoolBuilder(0, threadFactory)
                        .withMaximumPoolSize(Integer.MAX_VALUE, 60, TimeUnit.SECONDS))
                .withMetric(metricRegistry, "buffered_transactions_executor")
                .build();
    }

    @PreDestroy
    public void stop() throws Exception {
        final long waitingTime = BUFFER_TIMEOUT_UNIT.toSeconds(BUFFER_TIMEOUT)
                + AUTO_RELEASE_TIMEOUT_UNIT.toSeconds(AUTO_RELEASE_TIMEOUT) + EXECUTOR_SHUTDOWN_SLACK_SECONDS;

        ExecutorServiceUtils.shutdownExecutor("bufferedTransactionsExecutor", bufferedTransactionsExecutor,
                waitingTime, TimeUnit.SECONDS);
        bufferedTransactionsExecutor = null;
    }
}
