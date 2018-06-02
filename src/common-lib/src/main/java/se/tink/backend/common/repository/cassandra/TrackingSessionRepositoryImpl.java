package se.tink.backend.common.repository.cassandra;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;

import se.tink.backend.core.tracking.TrackingSession;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;

public class TrackingSessionRepositoryImpl implements TrackingSessionRepositoryCustom {

    private static final String TABLE_NAME = "tracking_sessions";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        HashMap<Object, Object> compression = Maps.newHashMap();
        compression.put(TableOption.CompressionOption.SSTABLE_COMPRESSION, "DeflateCompressor");

        final CreateTableSpecification tableSpecification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists().partitionKeyColumn("id", DataType.timeuuid()).column("userId", DataType.uuid())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy)
                .with(TableOption.COMPRESSION, compression);
        cassandraOperations.execute(tableSpecification);
    }

    @Override
    public TrackingSession findOne(UUID id) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("id", id));

        return cassandraOperations.selectOne(select, TrackingSession.class);
    }
}
