package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.exceptions.DriverInternalError;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.CategoryChangeRecord;

public class CategoryChangeRecordRepositoryImpl implements CategoryChangeRecordRepositoryCustom {
    private static final String TABLE_NAME = "transaction_category_change_records";

    // Recommended batch size: https://issues.apache.org/jira/browse/CASSANDRA-6487
    private static final int MAX_INSERT_BATCH_SIZE = 100;

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        ImmutableMap<Object, Object> compactionStrategy = ImmutableMap.of(CompactionOption.CLASS,
                "LeveledCompactionStrategy");

        final CreateTableSpecification categoryChangeRecordTableSpec = CreateTableSpecification
                .createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .partitionKeyColumn("transactionid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.timeuuid())
                .column("command", DataType.text())
                .column("newcategory", DataType.uuid())
                .column("oldcategory", DataType.uuid())
                .column("timestamp", DataType.timestamp())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(categoryChangeRecordTableSpec);
    }

    @Override
    public void truncate() {
        cassandraOperations.truncate(TABLE_NAME);
    }

    @Override
    public CategoryChangeRecord save(CategoryChangeRecord entity,
            long ttl, TimeUnit ttlUnit) {

        WriteOptions options = new WriteOptions();
        long seconds = ttlUnit.toSeconds(ttl);

        Preconditions.checkArgument(seconds < Integer.MAX_VALUE);
        options.setTtl((int) seconds);

        try {
            cassandraOperations.insert(entity, options);
        } catch (DriverInternalError e) {
            throw new RuntimeException("Could not save CategoryChangeRecord.", e);
        }

        return entity;
    }

    @Override
    public Iterable<CategoryChangeRecord> saveManySafely(
            Iterable<CategoryChangeRecord> entities) {

        for (List<CategoryChangeRecord> smallerBatch : Iterables
                .partition(entities, MAX_INSERT_BATCH_SIZE)) {
            try {
                cassandraOperations.insert(smallerBatch);
            } catch (DriverInternalError e) {
                throw new RuntimeException(
                        "Could not save CategoryChangeRecords. Possibly due to too big batch. Consider making batch smaller.",
                        e);
            }
        }

        return entities;
    }

    @Override
    public Iterable<CategoryChangeRecord> saveManySafely(Iterable<CategoryChangeRecord> entities,
            long ttl, TimeUnit ttlUnit) {

        WriteOptions options = new WriteOptions();
        long seconds = ttlUnit.toSeconds(ttl);

        Preconditions.checkArgument(seconds < Integer.MAX_VALUE);
        options.setTtl((int) seconds);

        for (List<CategoryChangeRecord> smallerBatch : Iterables
                .partition(entities, MAX_INSERT_BATCH_SIZE)) {
            try {
                cassandraOperations.insert(smallerBatch, options);
            } catch (DriverInternalError e) {
                throw new RuntimeException(
                        "Could not save CategoryChangeRecords. Possibly due to too big batch. Consider making batch smaller.",
                        e);
            }
        }

        return entities;
    }

    @Override
    public void deleteByUserIdAndId(UUID userId, UUID transactionId) {
        Delete statement = QueryBuilder.delete().from(TABLE_NAME);
        statement.setConsistencyLevel(ConsistencyLevel.ONE);
        statement.where(QueryBuilder.eq("userid", userId)).and(
                QueryBuilder.eq("transactionid", transactionId));
        cassandraOperations.execute(statement);
    }

    @Override
    public List<CategoryChangeRecord> findAllByUserIdAndTransactionId(UUID userId, UUID transactionId) {
        Select statement = QueryBuilder.select().from(TABLE_NAME);

        // This table has quick loose consistency requirements. More important that it's fast.
        statement.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

        statement.where(QueryBuilder.eq("userid", userId)).and(QueryBuilder.eq("transactionid", transactionId));

        return cassandraOperations.select(statement, CategoryChangeRecord.class);
    }

    @Override
    public boolean hasTtl(CategoryChangeRecord event) {
        Select statement = QueryBuilder.select().ttl("command").from(TABLE_NAME);

        // This table has quick loose consistency requirements. More important that it's fast.
        statement.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

        statement.where(QueryBuilder.eq("userid", event.getUserId()))
                .and(QueryBuilder.eq("transactionid", event.getTransactionId()))
                .and(QueryBuilder.eq("id", event.getId()));

        return !cassandraOperations.query(statement).one().isNull("TTL(command)");
    }

}
