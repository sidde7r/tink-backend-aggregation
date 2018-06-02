package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.CheckpointTransaction;

public class TransactionCheckpointRepositoryImpl implements TransactionCheckpointRepositoryCustom {
    private static final String TABLE_NAME = "transaction_checkpoints";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        HashMap<Object, Object> compression = Maps.newHashMap();
        compression.put(TableOption.CompressionOption.SSTABLE_COMPRESSION, "DeflateCompressor");

        final CreateTableSpecification tableSpecification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .partitionKeyColumn("checkpointid", DataType.text())
                .clusteredKeyColumn("transactionid", DataType.uuid())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy)
                .with(TableOption.COMPRESSION, compression);
        cassandraOperations.execute(tableSpecification);
    }

    @Override
    public void saveQuicklyWithTTL(CheckpointTransaction transactionSnapshot, long ttl, TimeUnit ttlUnit) {
        Preconditions.checkArgument(ttl > 0);

        WriteOptions options = new WriteOptions();
        long seconds = ttlUnit.toSeconds(ttl);

        Preconditions.checkArgument(seconds < Integer.MAX_VALUE);
        options.setTtl((int) seconds);

        // Speed is more important than consistency here.
        options.setConsistencyLevel(ConsistencyLevel.ONE);

        cassandraOperations.insert(transactionSnapshot, options);

    }

    @Override
    public List<CheckpointTransaction> findByUserIdAndCheckpointId(String userid, String checkpointid) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(com.datastax.driver.core.ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userid)));
        select.where(QueryBuilder.eq("checkpointid", checkpointid));
        return cassandraOperations.select(select, CheckpointTransaction.class);
    }
}
