package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.ProducedEventQueueActivity;
import se.tink.libraries.uuid.UUIDUtils;

public class ProducedEventQueueActivityRepositoryImpl implements ProducedEventQueueActivityRepositoryCustom {

    private static final int ROW_TTL_SECONDS = 60 * 60 * 24 * 30; // Time To Live: 30 days.
    private static final String TABLE_NAME = "produced_event_queue_activities";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public List<ProducedEventQueueActivity> findByUserId(String userId) {

        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));

        return cassandraOperations.select(select, ProducedEventQueueActivity.class);
    }

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpecification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("activitykey", DataType.text())
                .column("generated", DataType.timestamp())
                .with(TableOption.COMPACTION, compactionStrategy)

                // Add time to live to table. This will remove data automatically.
                .with("default_time_to_live", ROW_TTL_SECONDS, false, false);

        cassandraOperations.execute(tableSpecification);
    }
}
