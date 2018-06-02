package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;

public class NotificationEventRepositoryImpl implements NotificationEventRepositoryCustom {

    private static final int ROW_TTL_SECONDS = 60 * 60 * 24 * 14; // 14 days
    private static final String TABLE_NAME = "notifications_events";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification categoryChangeRecordTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("notificationid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.timeuuid())
                .column("date", DataType.timestamp())
                .column("generated", DataType.timestamp())
                .column("inserted", DataType.timestamp())
                .column("groupable", DataType.cboolean())
                .column("key", DataType.text())
                .column("message", DataType.text())
                .column("status", DataType.text())
                .column("title", DataType.text())
                .column("type", DataType.text())
                .column("url", DataType.text())
                .column("eventsource", DataType.text())

                // Add time to live to table, it will remove data automatically
                .with("default_time_to_live", ROW_TTL_SECONDS, false, false)

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(categoryChangeRecordTableSpec);
    }

}
