package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;

public class CheckpointRepositoryImpl implements CheckpointRepositoryCustom {
    private static final String TABLE_NAME = "checkpoints";

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
                .partitionKeyColumn("checkpointid", DataType.text())
                .clusteredKeyColumn("date", DataType.timestamp())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy)
                .with(TableOption.COMPRESSION, compression);
        cassandraOperations.execute(tableSpecification);
    }
}
