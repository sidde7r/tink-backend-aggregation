package se.tink.backend.common.repository.cassandra;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;

public class TrackingEventRepositoryImpl implements TrackingEventRepositoryCustom {

    private static final String TABLE_NAME = "tracking_events";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");
        
        HashMap<Object, Object> compression = Maps.newHashMap();
        compression.put(TableOption.CompressionOption.SSTABLE_COMPRESSION, "DeflateCompressor");

        final CreateTableSpecification tableSpecification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists().partitionKeyColumn("sessionId", DataType.uuid())
                .clusteredKeyColumn("id", DataType.timeuuid()).column("category", DataType.text())
                .column("action", DataType.text()).column("label", DataType.text()).column("value", DataType.bigint())
                .column("date", DataType.timestamp())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy)
                .with(TableOption.COMPRESSION, compression);
        cassandraOperations.execute(tableSpecification);
    }
}
