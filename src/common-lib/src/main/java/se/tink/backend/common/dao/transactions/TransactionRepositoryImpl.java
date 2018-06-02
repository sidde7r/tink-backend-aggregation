package se.tink.backend.common.dao.transactions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.joda.time.DateTime;
import se.tink.backend.common.repository.cassandra.CassandraPeriodByUserIdRepository;
import se.tink.backend.common.repository.cassandra.CassandraTransactionByUserIdAndPeriodRepository;
import se.tink.backend.common.utils.CassandraTransactionConverter;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.CassandraPeriodByUserId;
import se.tink.backend.core.CassandraTransactionByUserIdPeriod;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.libraries.uuid.UUIDUtils;

public class TransactionRepositoryImpl implements
        TransactionRepository {
    private static final Comparator<CassandraPeriodByUserId> periodComparator =
            Comparator.comparingInt(CassandraPeriodByUserId::getPeriod);

    private static final LogUtils log = new LogUtils(TransactionRepositoryImpl.class);
    private final CassandraTransactionByUserIdAndPeriodRepository cassandraTransactionRepository;
    private final CassandraPeriodByUserIdRepository cassandraPeriodRepository;
    private final MetricRegistry metricRegistry;
    private final int batchSize;

    @Inject
    public TransactionRepositoryImpl(
            CassandraTransactionByUserIdAndPeriodRepository cassandraTransactionRepository,
            CassandraPeriodByUserIdRepository cassandraPeriodRepository,
            MetricRegistry metricRegistry, @Named("distributedBatchSize") int batchSize) {
        this.cassandraTransactionRepository = Preconditions.checkNotNull(cassandraTransactionRepository);
        this.cassandraPeriodRepository = Preconditions.checkNotNull(cassandraPeriodRepository);
        this.metricRegistry = metricRegistry;
        this.batchSize = batchSize;
    }

    private Timer getTimerWithLabel(String label) {
        return metricRegistry.timer(MetricId.newId("transactions_by_userid_period_cassandra").label("action", label));
    }

    public void save(Transaction s) {
        Context time = getTimerWithLabel("save-single").time();
        cassandraTransactionRepository
                .saveByQuorum(CassandraTransactionConverter.toCassandraTransactionByUserIdAndPeriod(s));
        cassandraPeriodRepository.saveByQuorum(CassandraTransactionConverter.toCassandraPeriodByUserId(s));
        time.stop();
    }

    public void save(Iterable<Transaction> transactions) {
        if (Iterables.isEmpty(transactions)) {
            return;
        }

        final ImmutableListMultimap<String, ? extends Transaction> transactionByUserId = Multimaps.index(transactions,
                Transaction::getUserId);
        Context timerContext = getTimerWithLabel("save-multi").time();

        for (String userId : transactionByUserId.keySet()) {
            ImmutableList<? extends Transaction> userTransactions = transactionByUserId.get(userId);

            cassandraTransactionRepository.saveByQuorum(Lists.transform(userTransactions,
                    CassandraTransactionConverter.TO_CASSANDRA_BY_PERIOD), batchSize);
            Iterable<CassandraPeriodByUserId> periods = Iterables.transform(transactions,
                    CassandraTransactionConverter.TO_CASSANDRA_PERIOD_BY_USERID);
            cassandraPeriodRepository.saveByQuorum(periods, batchSize);
        }
        timerContext.stop();
    }

    public void delete(Iterable<Transaction> entities) {
        // Spring Data Cassandra < 1.1.0 might not handle deleting empty iterables. See
        // https://jira.spring.io/browse/DATACASS-148. Returning early here just in case.
        if (Iterables.isEmpty(entities)) {
            return;
        }

        ImmutableListMultimap<String, ? extends Transaction> transactionByUserId = Multimaps.index(entities,
                Transaction::getUserId);

        Context timerContext = getTimerWithLabel("delete-multi").time();
        for (String userId : transactionByUserId.keySet()) {
            ImmutableList<? extends Transaction> transactions = transactionByUserId.get(userId);

            cassandraTransactionRepository.deleteByQuorum(Lists.transform(transactions,
                    CassandraTransactionConverter.TO_CASSANDRA_BY_PERIOD), batchSize);
        }
        timerContext.stop();
    }

    public void deleteAll() {
        cassandraTransactionRepository.deleteAll();
        cassandraPeriodRepository.deleteAll();
    }

    @Override
    public Transaction findByUserIdAndId(String userId, int period, String transactionId) {
        Context timerContext = getTimerWithLabel("findByUserIdAndIdWithPeriods").time();
        CassandraTransactionByUserIdPeriod transaction = cassandraTransactionRepository
                .findByUserIdAndIdWithPeriod(userId, period, transactionId);
        return CassandraTransactionConverter.fromCassandraTransactionByUserIdAndPeriod(transaction);
    }

    @Override
    public List<Transaction> findByUserIdAndIds(String userId, List<String> transactionIds) {
        Context timerContext = getTimerWithLabel("findByUserIdAndIds").time();
        List<CassandraPeriodByUserId> periods = cassandraPeriodRepository.findByUserId(userId);
        try {
            List<CassandraTransactionByUserIdPeriod> byUserIdAndIds = cassandraTransactionRepository
                    .findByUserIdAndIdsWithPeriods(userId, transactionIds, periods);
            ArrayList<Transaction> transactions = Lists.newArrayList(Lists.transform(
                    byUserIdAndIds,
                    CassandraTransactionConverter.FROM_CASSANDRA_BY_PERIOD));

            return transactions;
        } catch (InterruptedException | ExecutionException e) {
            log.warn(userId, "Async fetching of transactions failed", e);
            throw new RuntimeException(e);
        } finally {
            timerContext.stop();
        }
    }

    @Override
    public List<Transaction> findAllByUserId(String userId) {
        Context timerContext = getTimerWithLabel("findByUserIdWithPeriods").time();
        List<Integer> periods = cassandraPeriodRepository.findByUserId(userId).stream()
                .map(CassandraPeriodByUserId::getPeriod).collect(Collectors.toList());

        List<Transaction> transactions = findTransactions(userId, periods);
        timerContext.stop();
        return transactions;
    }

    @Override
    public List<Transaction> findByUserIdAndTime(String userId, DateTime startDate, DateTime endDate) {
        Context timerContext = getTimerWithLabel("findByUserIdAndTime").time();
        List<Integer> periods = DateUtils
                .getYearMonthPeriods(YearMonth.of(startDate.getYear(), startDate.getMonthOfYear()),
                        YearMonth.of(endDate.getYear(), endDate.getMonthOfYear()));
        List<Integer> cassandraPeriods = cassandraPeriodRepository.findByUserId(userId).stream()
                .map(CassandraPeriodByUserId::getPeriod).collect(Collectors.toList());

        // If there is less in cassandra we want to use less
        periods.retainAll(cassandraPeriods);

        List<Transaction> transactions = findTransactions(userId, periods);
        timerContext.stop();
        return transactions;
    }

    private List<Transaction> findTransactions(String userId, List<Integer> periods) {
        try {
            ArrayList<Transaction> transactions = Lists.newArrayList(Lists.transform(
                    cassandraTransactionRepository.findByUserIdWithPeriods(userId, periods),
                    CassandraTransactionConverter.FROM_CASSANDRA_BY_PERIOD));
            return transactions;
        } catch (InterruptedException | ExecutionException e) {
            log.warn(userId, "Async fetching of transactions failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Transaction> findOneOptionallyByUserIdAndId(String userId, int period, String transactionId) {
        final Transaction transaction = findByUserIdAndId(userId, period, transactionId);
        return Optional.ofNullable(transaction);
    }

    @Override
    public Transaction findOneByUserIdAndIds(String userId, String transactionId) {
        final List<Transaction> list = findByUserIdAndIds(userId, Lists.newArrayList(transactionId));

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    @Override
    public int countByUserId(String userId) {
        Context timerContext = getTimerWithLabel("countByUserId").time();
        int count = cassandraTransactionRepository.countByUserId(UUIDUtils.fromTinkUUID(userId)).intValue();
        timerContext.stop();
        return count;
    }

    @Override
    public void updateCategory(String userId, Map<String, Integer> transactionIdsToPeriods,
            Category category) {
        Context timerContext = getTimerWithLabel("updateUserModifiedCategory").time();

        cassandraTransactionRepository.updateUserModifiedCategory(userId, transactionIdsToPeriods, category);
        timerContext.stop();
    }

    @Override
    public void updateMerchantIdAndDescription(String userId, Map<String, Integer> transactionIdsToPeriods,
            String merchantId, String merchantName) {
        Context timerContext = getTimerWithLabel("updateMerchantIdAndDescription").time();

        cassandraTransactionRepository
                .updateMerchantIdAndDescription(userId, transactionIdsToPeriods, merchantId, merchantName);
        timerContext.stop();
    }

    @Override
    public void deleteByUserIdAndCredentials(String userId, String credentialsId) {
        Context timerContext = getTimerWithLabel("deleteByUserIdAndCredentials").time();
        cassandraTransactionRepository.deleteByUserIdAndCredentials(userId, credentialsId);
        timerContext.stop();
    }

    @Override
    public void deleteByUserIdAndAccountId(String userId, String accountId) {
        Context timerContext = getTimerWithLabel("deleteByUserIdAndAccountId").time();
        cassandraTransactionRepository.deleteByUserIdAndAccountId(userId, accountId);
        timerContext.stop();
    }

    @Override
    public void deleteByUserId(String userId) {
        Context timerContext = getTimerWithLabel("deleteByUser").time();
        // PUL requires us to actually delete the full user data.
        cassandraTransactionRepository.deleteByUserId(userId);
        cassandraPeriodRepository.deleteByUserId(userId);
        timerContext.stop();
    }

    @Override
    public void deleteByUserIdAndId(String userId, String id) {
        Context timerContext = getTimerWithLabel("deleteByUserIdAndId").time();
        cassandraTransactionRepository.deleteByUserIdAndId(userId, id);
        timerContext.stop();
    }

    @Override
    public void check() throws Exception {
        // We expect this to throw exception on error.
        // The 000000… is a UUID in non-dash-format that we expect not to have any transactions.
        List<Transaction> transactions = findTransactions("00000000000000000000000000000000",
                Collections.singletonList(0000));

        Preconditions.checkNotNull(transactions);
    }
}
