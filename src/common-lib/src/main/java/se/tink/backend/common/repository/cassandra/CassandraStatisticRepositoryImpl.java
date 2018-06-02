package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.CassandraStatistic;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraStatisticRepositoryImpl implements CassandraStatisticRepositoryCustom {
    private static final String TABLE_NAME = "statistics";
    private static final LogUtils log = new LogUtils(CassandraStatisticRepositoryImpl.class);

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public List<CassandraStatistic> findStatistics(String userId, List<ResolutionTypes> resolutions) {
        try {
            final List<CompletableFuture<List<CassandraStatistic>>> futures =
                    Lists.newArrayListWithExpectedSize(resolutions.size());
            List<String> stringResolutions = resolutions.stream().map(r -> r.name()).collect(Collectors.toList());

            for (String resolution : stringResolutions) {
                Select select = QueryBuilder.select().from(TABLE_NAME);
                select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
                select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                        .and(QueryBuilder.eq("resolution", resolution));

                futures.add(CompletableFuture.supplyAsync(() ->
                        cassandraOperations.select(select, CassandraStatistic.class)));
            }

            return joinListOfFutures(futures).stream().reduce(Collections.emptyList(),
                    (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList()));
        } catch (InterruptedException|ExecutionException e) {
            log.warn(userId, "Async fetching of statistics failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CassandraStatistic> findStatistics(String userId, List<ResolutionTypes> resolutions,
            List<Integer> periodHeads) {
        try {
            final List<CompletableFuture<List<CassandraStatistic>>> futures =
                        Lists.newArrayListWithExpectedSize(resolutions.size());
                List<String> stringResolutions = resolutions.stream().map(r -> r.name()).collect(Collectors.toList());
            List<Integer> orderedPeriods = periodHeads.stream().sorted().collect(Collectors.toList());
            for (String resolution : stringResolutions) {
                    final Select select = QueryBuilder.select().from(TABLE_NAME);
                    select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
                    select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                            .and(QueryBuilder.eq("resolution", resolution))
                            .and(QueryBuilder.gte("periodhead", orderedPeriods.get(0)))
                            .and(QueryBuilder.lte("periodhead", orderedPeriods.get(orderedPeriods.size()-1)));

                    futures.add(CompletableFuture.supplyAsync(() ->
                            cassandraOperations.select(select, CassandraStatistic.class)));
                }

                return joinListOfFutures(futures).stream().reduce(Collections.emptyList(),
                        (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toList()));
        } catch (InterruptedException|ExecutionException e) {
            log.warn(userId, "Async fetching of statistics failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<CassandraStatistic> findStatistics(String userId, int periodHead,
            String resolution) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.eq("resolution", resolution))
                .and(QueryBuilder.gte("periodhead", periodHead));
        return cassandraOperations.select(select, CassandraStatistic.class);
    }

    @Override
    public <S extends CassandraStatistic> void saveByQuorum(Iterable<S> entities) {
        WriteOptions options = new WriteOptions();
        options.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        for (List<S> batch : Iterables.partition(entities, 10)) {
            cassandraOperations.insert(batch, options);
        }
    }

    @Override
    public <S extends CassandraStatistic> S saveByQuorum(S entity) {
        WriteOptions options = new WriteOptions();
        options.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        return cassandraOperations.insert(entity, options);
    }

    @Override
    public void deleteByUserIdAndResolution(String userId, ResolutionTypes resolution) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.eq("resolution", resolution.name()));
        cassandraOperations.execute(delete);
    }

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification transactionTableSpec = CreateTableSpecification
                .createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .partitionKeyColumn("resolution", DataType.text())
                .clusteredKeyColumn("periodhead", DataType.cint())
                .clusteredKeyColumn("type", DataType.text())
                .column("data", DataType.blob())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(transactionTableSpec);
    }

    private <T> List<T> joinListOfFutures(List<CompletableFuture<T>> futures) throws InterruptedException, ExecutionException {
        return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
    }
}
