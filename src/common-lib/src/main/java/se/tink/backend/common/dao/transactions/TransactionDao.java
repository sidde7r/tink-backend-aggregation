package se.tink.backend.common.dao.transactions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.joda.time.DateTime;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.concurrency.TypedThreadPoolBuilder;
import se.tink.backend.common.concurrency.WrappedRunnableListenableFutureTask;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.health.Checkable;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.repository.elasticsearch.TransactionSearchIndex;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.CassandraTransactionConverter;
import se.tink.backend.common.utils.ExecutorServiceUtils;
import se.tink.backend.core.CassandraTransactionDeleted;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.uuid.UUIDUtils;

public class TransactionDao implements Managed, Checkable {
    private static final LogUtils log = new LogUtils(TransactionDao.class);
    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("thread-repository-impl-executor-thread-%d")
            .build();
    private static final int NTHREADS = 20;
    private static final MetricId UPDATE_CATEGORY_METRIC = MetricId.newId("persist_categories");

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CassandraTransactionDeletedRepository cassandraTransactionDeletedRepository;
    private final UserRepository userRepository;
    private final TransactionSearchIndex transactionSearchIndex;
    private final TransactionEnricher transactionEnricher;
    private final CategoryChangeRecordDao categoryChangeRecordDao;
    private final TransactionCleaner transactionCleaner;
    private final MetricRegistry metricRegistry;

    private ListenableThreadPoolExecutor<Runnable> executorService;

    private final Timer updateCategoryTimer;

    @Inject
    public TransactionDao(CategoryRepository categoryRepository,
            TransactionRepository transactionRepository,
                          AccountRepository accountRepository,
                          CassandraTransactionDeletedRepository cassandraTransactionDeletedRepository,
                          UserRepository userRepository,
                          TransactionSearchIndex transactionSearchIndex, TransactionEnricher transactionEnricher,
                          TransactionCleaner transactionCleaner, MetricRegistry registry,
                          CategoryChangeRecordDao categoryChangeRecordDao) {

        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.cassandraTransactionDeletedRepository = cassandraTransactionDeletedRepository;
        this.userRepository = userRepository;
        this.transactionSearchIndex = transactionSearchIndex;
        this.transactionEnricher = transactionEnricher;
        this.transactionCleaner = transactionCleaner;
        this.metricRegistry = registry;
        this.categoryChangeRecordDao = categoryChangeRecordDao;

        this.updateCategoryTimer = metricRegistry.timer(UPDATE_CATEGORY_METRIC);
    }

    public void index(Collection<Transaction> transactions, boolean sync) {
        if (transactions.isEmpty()) {
            log.info("Not indexing empty transaction set");
            return;
        }

        transactionSearchIndex.index(transactions, sync, categoryIdToCategory());
    }

    @VisibleForTesting
    Function<String, Optional<Category>> categoryIdToCategory() {
        return categoryId -> {
            if (!Strings.isNullOrEmpty(categoryId)) {
                return Optional.ofNullable(categoryRepository
                        .getCategoriesById(categoryRepository.getDefaultLocale())
                        .get(categoryId));
            }
            return Optional.empty();
        };
    }

    @Override
    public void check() throws Exception {
        transactionRepository.check();
    }

    public List<Transaction> findAllByUser(@Nonnull User user) {
        return transactionRepository.findAllByUserId(user.getId());
    }

    public int countByUser(@Nonnull User user) {
        return transactionRepository.countByUserId(user.getId());
    }

    public int countByUserId(@Nonnull String userId) {
        return countByUser(userRepository.findOne(userId));
    }

    public List<Transaction> findByUserIdAndId(@Nonnull User user, List<String> transactionIds) {
        return transactionRepository.findByUserIdAndIds(user.getId(), transactionIds);
    }

    public List<Transaction> findAllByUserId(String userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    public List<Transaction> findAllByUserIdAndTime(String userId, DateTime startDate, DateTime endDate) {
        return transactionRepository.findByUserIdAndTime(userId, startDate, endDate);
    }

    public Transaction findOneByUserIdAndId(String userId, String transactionId,
            Optional<Integer> period) {
        if (period.isPresent()) {
            Optional<Transaction> transaction = transactionRepository
                    .findOneOptionallyByUserIdAndId(userId, period.get(), transactionId);
            if (transaction.isPresent()) {
                return transaction.get();
            }
        }
        return transactionRepository.findOneByUserIdAndIds(userId, transactionId);
    }

    public Transaction findOneByUserAndId(@Nonnull User user, String transactionId) {
        return transactionRepository.findOneByUserIdAndIds(user.getId(), transactionId);
    }

    public void updateMerchantIdAndDescription(@Nonnull User user, Map<String, Integer> transactionIdsToPeriods,
            String merchantId,
            String merchantName) {
        List<String> transactionIds = new ArrayList<>(transactionIdsToPeriods.keySet());

        transactionRepository
                .updateMerchantIdAndDescription(user.getId(), transactionIdsToPeriods, merchantId, merchantName);
    }

    public void updateCategoryByIdsAndUser(@Nonnull User user, Map<String, Integer> transactionIdsToPeriods,
            Category category) {
        Timer.Context updateTimer = updateCategoryTimer.time();
        try {
            List<String> transactionIds = new ArrayList<>(transactionIdsToPeriods.keySet());

            transactionRepository.updateCategory(user.getId(), transactionIdsToPeriods, category);

        } finally {
            updateTimer.stop();
        }
    }

    public void saveAndIndex(String userId, final Collection<Transaction> transactions, boolean sync) {
        for (Transaction transaction : transactions) {
            transactionEnricher.enrich(transaction);
            transactionCleaner.clean(transaction);
        }
        transactionRepository.save(transactions);

        if (sync || transactions.size() == 1) {
            // Flush to search index synchronously.
            index(transactions, true);
        } else {
            // Flush to search index eventually.
            executorService.execute(() -> index(transactions, false));
        }
    }

    public void save(@Nonnull User user, Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public void save(@Nonnull User user, Iterable<Transaction> transactions) {
        transactionRepository.save(transactions);
    }

    public void save(String userId, Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public void save(String userId, Iterable<Transaction> transactions) {
        transactionRepository.save(transactions);
    }

    public void saveAndIndex(@Nonnull User user, final Collection<Transaction> transactions, boolean sync) {
        for (Transaction transaction : transactions) {
            transactionEnricher.enrich(transaction);
            transactionCleaner.clean(transaction);
        }

        transactionRepository.save(transactions);

        if (sync || transactions.size() == 1) {
            // Flush to search index synchronously.
            index(transactions, true);
        } else {
            // Flush to search index eventually.
            executorService.execute(() -> index(transactions, false));
        }
    }

    @Override
    public void start() {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue = Queues
                .newLinkedBlockingQueue();

        executorService = ListenableThreadPoolExecutor
                .builder(executorServiceQueue, new TypedThreadPoolBuilder(NTHREADS, threadFactory))
                .withMetric(metricRegistry, "indexedtransaction_dao_executor")
                .build();
    }

    @Override
    public void stop() {
        ExecutorServiceUtils.shutdownExecutor("TransactionDao#executorService", executorService, 60, TimeUnit.SECONDS);
        executorService = null;
    }

    public void deleteAll() {
        transactionRepository.deleteAll();
    }

    public void deleteByUserId(@Nonnull String userId) {
        transactionRepository.deleteByUserId(userId);
        transactionSearchIndex.deleteByUserId(userId);
    }

    public void deleteByUserIdAndCredentials(@Nonnull User user, String credentialId) {
        transactionRepository.deleteByUserIdAndCredentials(user.getId(), credentialId);
        transactionSearchIndex.deleteByUserIdAndCredentialId(user.getId(), credentialId);
    }

    public void delete(Iterable<Transaction> transactionsToDelete) {
        if (Iterables.isEmpty(transactionsToDelete)) {
            return;
        }

        transactionRepository.delete(transactionsToDelete);

        saveDeletedTransactions(transactionsToDelete);
        transactionSearchIndex.delete(transactionsToDelete);
        transactionsToDelete.forEach(t -> categoryChangeRecordDao.deleteByUserIdAndId(
                UUIDUtils.fromTinkUUID(t.getUserId()),
                UUIDUtils.fromTinkUUID(t.getId())
        ));
    }

    public void deleteByUserIdAndAccountId(@Nonnull String userId, String accountId) {
        transactionRepository.deleteByUserIdAndAccountId(userId, accountId);
        transactionSearchIndex.deleteByUserIdAndAccountId(userId, accountId);
    }

    public void deleteByAccountIds(List<String> accountIds) {
        if (accountIds.isEmpty()) {
            return;
        }

        for(String accountId : accountIds) {
            String userId = accountRepository.findOne(accountId).getUserId();
            deleteByUserIdAndAccountId(userId, accountId);
        }
    }

    public void deleteByUserIdAndTransactionId(String userId, String transactionId) {
        transactionRepository.deleteByUserIdAndId(userId, transactionId);

        transactionSearchIndex.deleteByUserIdAndId(userId, transactionId);
        categoryChangeRecordDao
                .deleteByUserIdAndId(UUIDUtils.fromTinkUUID(userId), UUIDUtils.fromTinkUUID(transactionId));
    }

    private void saveDeletedTransactions(Iterable<Transaction> deletedTransactions) {
        Iterable<CassandraTransactionDeleted> transactionsDeleted = Iterables.transform(deletedTransactions,
                transaction -> {
                    CassandraTransactionDeleted transactionDeleted = CassandraTransactionConverter
                            .toCassandraTransactionDeleted(transaction);
                    transactionDeleted.setDeleted(new Date());
                    return transactionDeleted;
                });

        cassandraTransactionDeletedRepository.saveInBatches(transactionsDeleted);
    }
}
