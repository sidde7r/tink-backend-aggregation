package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.QueryOptions;
import org.springframework.cassandra.core.QueryOptions.QueryOptionsBuilder;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cassandra.core.WriteOptions.WriteOptionsBuilder;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.CassandraPeriodByUserId;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraPeriodByUserIdRepositoryImpl implements CassandraPeriodByUserIdRepositoryCustom {

    @Autowired
    CassandraOperations cassandraOperations;

    private static final String TABLE_NAME = "period_by_userid";

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification periodTableSpec = CreateTableSpecification
                .createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("period", DataType.cint())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(periodTableSpec);
    }

    @Override
    public void deleteByUserId(String userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));

        cassandraOperations.executeAsynchronously(delete);
    }

    @Override
    public void deleteByUserIdAndPeriod(String userId, int period) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.eq("period", period));

        cassandraOperations.executeAsynchronously(delete);
    }

    @Override
    public List<CassandraPeriodByUserId> findByUserId(String userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));

        return cassandraOperations.select(select, CassandraPeriodByUserId.class);
    }

    @Override
    public List<CassandraPeriodByUserId> findByUserIdAndPeriod(String userId, int period) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
                .and(QueryBuilder.eq("period", period));

        return cassandraOperations.select(select, CassandraPeriodByUserId.class);
    }

    @Override
    public <S extends CassandraPeriodByUserId> S saveByQuorum(S entity) {
        WriteOptionsBuilder options = WriteOptions.builder();
        options.consistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        cassandraOperations.insert(entity, options.build());
        return entity;
    }

    @Override
    public <S extends CassandraPeriodByUserId> Iterable<S> saveByQuorum(Iterable<S> entities, int batchSize) {
        WriteOptionsBuilder builder = WriteOptions.builder();
        builder.consistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        WriteOptions options = builder.build();
        for (List<S> batch : Iterables.partition(entities, batchSize)) {
            cassandraOperations.insert(batch, options);
        }
        return entities;
    }

    @Override
    public void deleteByQuorum(Iterable<? extends CassandraPeriodByUserId> entities, int batchSize) {
        QueryOptionsBuilder builder = QueryOptions.builder();
        builder.consistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        QueryOptions options = builder.build();
        for (List<? extends CassandraPeriodByUserId> batch : Iterables.partition(entities, batchSize)) {
            cassandraOperations.delete(batch, options);
        }
    }

    @Override
    public void check() throws Exception {

    }
}
