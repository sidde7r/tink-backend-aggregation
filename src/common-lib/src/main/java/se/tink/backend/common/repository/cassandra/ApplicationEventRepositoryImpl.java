package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.application.ApplicationEvent;

public class ApplicationEventRepositoryImpl implements ApplicationEventRepositoryCustom {
    private static final String TABLE_NAME = "applications_events";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        CreateTableSpecification categoryChangeRecordTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("applicationid", DataType.uuid(), Ordering.ASCENDING)
                .clusteredKeyColumn("id", DataType.timeuuid(), Ordering.DESCENDING)
                .column("applicationtype", DataType.text())
                .column("applicationstatus", DataType.text())
                .column("applicationupdated", DataType.timestamp())
                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(categoryChangeRecordTableSpec);
    }

    @Override
    public List<ApplicationEvent> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", userId));

        // Default consistency level for Datastax Java driver is ONE. Just making it explicit here.
        select.setConsistencyLevel(ConsistencyLevel.ONE);

        return cassandraOperations.select(select, ApplicationEvent.class);
    }

    @Override
    public List<ApplicationEvent> findMostRecentByUserId(UUID userId, int limit) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", userId));
        select.limit(limit);

        // Default consistency level for Datastax Java driver is ONE. Just making it explicit here.
        select.setConsistencyLevel(ConsistencyLevel.ONE);

        return cassandraOperations.select(select, ApplicationEvent.class);
    }

    @Override
    public List<ApplicationEvent> findMostRecentByUserIdAndApplicationId(UUID userId, UUID applicationId, int limit) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("applicationid", applicationId));
        select.limit(limit);

        // Default consistency level for Datastax Java driver is ONE. Just making it explicit here.
        select.setConsistencyLevel(ConsistencyLevel.ONE);

        return cassandraOperations.select(select, ApplicationEvent.class);
    }
}
