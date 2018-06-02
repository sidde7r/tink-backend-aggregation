package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.QueryOptions;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.CassandraPeriodByUserId;
import se.tink.backend.core.CassandraTransactionByUserIdPeriod;
import se.tink.backend.core.Category;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraTransactionByUserIdAndPeriodRepositoryImpl
        implements CassandraTransactionByUserIdAndPeriodRepositoryCustom {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @VisibleForTesting
    protected static final String TABLE_NAME = "transactions_by_userid_period";
    private static final List<Integer> allPeriods = DateUtils.getYearMonthPeriods(YearMonth.of(1970, 1), YearMonth.now().plusYears(1));

    @Autowired
    CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification transactionTableSpec = CreateTableSpecification
                .createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .partitionKeyColumn("period", DataType.cint())
                .clusteredKeyColumn("id", DataType.uuid())
                .column("accountid", DataType.uuid())
                .column("exactamount", DataType.decimal())
                .column("categoryId", DataType.uuid())
                .column("categoryType", DataType.text())
                .column("credentialsId", DataType.uuid())
                .column("date", DataType.timestamp())
                .column("description", DataType.text())
                .column("formatteddescription", DataType.text())
                .column("inserted", DataType.bigint())
                .column("internalpayloadserialized", DataType.text())
                .column("lastmodified", DataType.timestamp())
                .column("merchantId", DataType.uuid())
                .column("notes", DataType.text())
                .column("exactoriginalamount", DataType.decimal())
                .column("originaldate", DataType.timestamp())
                .column("originaldescription", DataType.text())
                .column("partsserialized", DataType.text())
                .column("payloadserialized", DataType.text())
                .column("pending", DataType.cboolean())
                .column("timestamp", DataType.bigint())
                .column("type", DataType.text())
                .column("usermodifiedamount", DataType.cboolean())
                .column("usermodifiedcategory", DataType.cboolean())
                .column("usermodifieddate", DataType.cboolean())
                .column("usermodifieddescription", DataType.cboolean())
                .column("usermodifiedlocation", DataType.cboolean())
                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(transactionTableSpec);
    }

    @Override
    public void deleteByUserId(String userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.in("period", allPeriods));
        cassandraOperations.executeAsynchronously(delete);
    }

    @Override
    public void deleteByUserIdAndId(String userId, String id) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.in("period", allPeriods))
                .and(QueryBuilder.eq("id", UUIDUtils.fromTinkUUID(id)));
        cassandraOperations.executeAsynchronously(delete);
    }

    @Override
    public void deleteByUserIdAndPeriodAndId(String userId, int period, String id) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.eq("period", period))
                .and(QueryBuilder.eq("id", UUIDUtils.fromTinkUUID(id)));
        cassandraOperations.executeAsynchronously(delete);
    }

    @Override
    public void deleteByUserIdAndCredentials(String userId, String credentialsId) {

        Select select = QueryBuilder.select("userid", "period", "id", "credentialsid").from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.in("period", allPeriods));
        List<CassandraTransactionByUserIdPeriod> transactions = cassandraOperations
                .select(select, CassandraTransactionByUserIdPeriod.class);
        List<CassandraTransactionByUserIdPeriod> transactionsToDelete = transactions.stream()
                .filter(t -> credentialsId.equals(UUIDUtils.toTinkUUID(t.getCredentialsId()))).collect(Collectors.toList());
        deleteTransactions(transactionsToDelete);
    }

    @Override
    public void deleteByUserIdAndAccountId(String userId, String accountId) {
        Select select = QueryBuilder.select("userid", "period", "id", "accountid").from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.in("period", allPeriods));
        List<CassandraTransactionByUserIdPeriod> transactions = cassandraOperations
                .select(select, CassandraTransactionByUserIdPeriod.class);
        List<CassandraTransactionByUserIdPeriod> transactionsToDelete = transactions.stream()
                .filter(t -> accountId.equals(UUIDUtils.toTinkUUID(t.getAccountId()))).collect(Collectors.toList());
        deleteTransactions(transactionsToDelete);
    }

    @Override
    public CassandraTransactionByUserIdPeriod findByUserIdAndIdWithPeriod(String userId, int period, String transactionId) {
        final Select select = selectQueryBuilder(
                Optional.of(userId), Optional.of(transactionId), Optional.empty(), Optional.of(period), Optional.empty());
        return cassandraOperations.selectOne(select, CassandraTransactionByUserIdPeriod.class);
    }

    private void deleteTransactions(List<CassandraTransactionByUserIdPeriod> transactionsToDelete) {
        transactionsToDelete.stream().<Runnable>map(t ->
                () -> deleteByUserIdAndPeriodAndId(
                        UUIDUtils.toTinkUUID(t.getUserId()),
                        t.getPeriod(),
                        UUIDUtils.toTinkUUID(t.getId())))
                .forEach(executor::submit);
    }

    @Override
    public List<CassandraTransactionByUserIdPeriod> findLastYearToNextYearByUserIdWithPeriods(
            String userId, int maxPeriod) throws ExecutionException, InterruptedException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth max = YearMonth.parse(Integer.toString(maxPeriod), formatter);
        List<Integer> periods = DateUtils.getYearMonthPeriods(YearMonth.now().minusYears(1).minusMonths(1), max);

        final List<CompletableFuture<List<CassandraTransactionByUserIdPeriod>>> futures =
                Lists.newArrayListWithExpectedSize(periods.size());
        for (int p : periods) {
            final Select select = selectQueryBuilder(
                    Optional.of(userId), Optional.empty(), Optional.empty(), Optional.of(p), Optional.empty());
            futures.add(CompletableFuture.supplyAsync(() ->
                cassandraOperations.select(select, CassandraTransactionByUserIdPeriod.class)));
        }

        return futureFromListOfFutures(futures).get().stream()
                .reduce(Collections.emptyList(),
                        (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList()));
    }

    @Override
    public List<CassandraTransactionByUserIdPeriod> findByUserIdAndIdsWithPeriods(String userId, List<String> ids,
            List<CassandraPeriodByUserId> periods) throws ExecutionException, InterruptedException {
        final List<CompletableFuture<List<CassandraTransactionByUserIdPeriod>>> futures =
                Lists.newArrayListWithExpectedSize(periods.size());
        for (CassandraPeriodByUserId p : periods) {
            final Select select = selectQueryBuilder(
                    Optional.of(userId), Optional.empty(), Optional.of(ids), Optional.of(p.getPeriod()), Optional.empty());
            futures.add(CompletableFuture.supplyAsync(() ->
                    cassandraOperations.select(select, CassandraTransactionByUserIdPeriod.class)));
        }

        return futureFromListOfFutures(futures).get().stream()
                .reduce(Collections.emptyList(),
                        (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList()));
    }

    @Override
    public List<CassandraTransactionByUserIdPeriod> findByUserIdWithPeriods(String userId,
            List<Integer> periods) throws InterruptedException, ExecutionException {
        final List<CompletableFuture<List<CassandraTransactionByUserIdPeriod>>> futures =
                Lists.newArrayListWithExpectedSize(periods.size());
        for (Integer p : periods) {
            final Select select = selectQueryBuilder(
                    Optional.of(userId), Optional.empty(), Optional.empty(), Optional.of(p), Optional.empty());
            futures.add(CompletableFuture.supplyAsync(() ->
                    cassandraOperations.select(select, CassandraTransactionByUserIdPeriod.class)));
        }

        return futureFromListOfFutures(futures).get().stream()
                .reduce(Collections.emptyList(),
                        (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList()));
    }

    @VisibleForTesting
    protected Select selectQueryBuilder(Optional<String> userId, Optional<String> transactionId, Optional<List<String>> transactionIds, Optional<Integer> period,
            Optional<List<Integer>> inPeriods) {
        final Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        userId.ifPresent(u -> select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(u))));
        if (transactionId.isPresent()) {
            select.where(QueryBuilder.eq("id", UUIDUtils.fromTinkUUID(transactionId.get())));
        } else if(transactionIds.isPresent()) {
            select.where(QueryBuilder.in("id",
                    Lists.transform(transactionIds.get(), UUIDUtils.FROM_TINK_UUID_TRANSFORMER)));
        }
        if (period.isPresent()) {
            select.where(QueryBuilder.eq("period", period.get()));
        } else if (inPeriods.isPresent()) {
            select.where(QueryBuilder.in("period", inPeriods.get()));
        }
        return select;
    }

    private <T> CompletableFuture<List<T>> futureFromListOfFutures(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                futures.stream()
                        .map(future -> future.join())
                        .collect(Collectors.<T>toList()));
    }

    @Override
    public Long countByUserId(UUID userId) {
        Select select = QueryBuilder.select().countAll().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", userId)).and(
                QueryBuilder.in("period", allPeriods));
        return cassandraOperations.query(select).one().getLong(0);
    }

    /**
     * Should be called only in the context of users
     * updating their transactions
     */
    @Override
    public void updateUserModifiedCategory(String userId, Map<String, Integer> transactionIdsToPeriods,
            Category category) {
        transactionIdsToPeriods.forEach((transactionIds, period) -> {
            Update update = QueryBuilder.update(TABLE_NAME);
            update.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
            update.with(QueryBuilder.set("categoryid", UUIDUtils.fromTinkUUID(category.getId())));
            update.with(QueryBuilder.set("usermodifiedcategory", true));
            update.with(QueryBuilder.set("categorytype", category.getType().toString()));
            update.with(QueryBuilder.set("lastmodified", new Date()));
            update.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                    .and(QueryBuilder.eq("period", period))
                    .and(QueryBuilder.eq("id", UUIDUtils.fromTinkUUID(transactionIds)));
            cassandraOperations.execute(update);
        });
    }

    @Override
    public void updateMerchantIdAndDescription(String userId, Map<String, Integer> transactionIdsToPeriods,
            String merchantId, String merchantName) {
        transactionIdsToPeriods.forEach((transactionIds, period) -> {
            Update update = QueryBuilder.update(TABLE_NAME);
            update.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
            update.with(QueryBuilder.set("merchantid", UUIDUtils.fromTinkUUID(merchantId)));
            update.with(QueryBuilder.set("description", merchantName));
            update.with(QueryBuilder.set("usermodifiedlocation", true));
            update.with(QueryBuilder.set("lastmodified", new Date()));
            update.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                    .and(QueryBuilder.eq("period", period))
                    .and(QueryBuilder.eq("id", UUIDUtils.fromTinkUUID(transactionIds)));

            cassandraOperations.execute(update);
        });
    }

    @Override
    public <S extends CassandraTransactionByUserIdPeriod> S saveByQuorum(S entity) {
        WriteOptions options = new WriteOptions();
        options.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        cassandraOperations.insert(entity, options);
        return entity;
    }

    @Override
    public <S extends CassandraTransactionByUserIdPeriod> Iterable<S> saveByQuorum(Iterable<S> entities,
            int batchSize) {
        WriteOptions options = new WriteOptions();
        options.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        for (List<S> batch : Iterables.partition(entities, batchSize)) {
            cassandraOperations.insert(batch, options);
        }
        return entities;
    }

    @Override
    public void deleteByQuorum(Iterable<? extends CassandraTransactionByUserIdPeriod> entities, int batchSize) {
        QueryOptions options = new QueryOptions();
        options.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        for (List<? extends CassandraTransactionByUserIdPeriod> batch : Iterables.partition(entities, batchSize)) {
            cassandraOperations.delete(batch, options);
        }
    }

    public void deleteAll() {
        cassandraOperations.truncate(TABLE_NAME);
    }
}
