package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;

public class CredentialsEventRepositoryImpl implements CredentialsEventRepositoryCustom {

    private static final String TABLE_NAME = "credentials_events";

    @Autowired
    private CassandraOperations cassandraOperations;
    
    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification categoryChangeRecordTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("credentialsid", DataType.uuid(), Ordering.ASCENDING)
                .clusteredKeyColumn("id", DataType.timeuuid(), Ordering.DESCENDING)
                .column("message", DataType.text())
                .column("providername", DataType.text())
                .column("status", DataType.text())
                .column("timestamp", DataType.timestamp())
                .column("refreshtype", DataType.text())
                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(categoryChangeRecordTableSpec);
    }
    
    @Override
    public List<CredentialsEvent> findByUserIdAndCredentialsId(String userId, String credentialsId) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        select.where(QueryBuilder.eq("credentialsid", UUIDUtils.fromTinkUUID(credentialsId)));
        
        // Default consistency level for Datastax Java driver is ONE. Just making it explicit here.
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        
        return cassandraOperations.select(select, CredentialsEvent.class);
    }

    /**
     * Note: This method delivers `MostRecent` by UserId and CredentialsId under the assumption that the table is
     * already ordered by `id DESC` as default.
     */
    @Override
    public List<CredentialsEvent> findByUserId(String userId) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));

        // Default consistency level for Datastax Java driver is ONE. Just making it explicit here.
        select.setConsistencyLevel(ConsistencyLevel.ONE);

        return cassandraOperations.select(select, CredentialsEvent.class);
    }

    @Override
    public List<CredentialsEvent> findMostRecentByUserIdAndCredentialsId(String userId, String credentialsId, int limit) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        select.where(QueryBuilder.eq("credentialsid", UUIDUtils.fromTinkUUID(credentialsId)));
        select.limit(limit);
        
        // Default consistency level for Datastax Java driver is ONE. Just making it explicit here.
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        
        return cassandraOperations.select(select, CredentialsEvent.class);
    }

    @Override
    public List<CredentialsEvent> findMostRecentByUserIdAndCredentialsIdAndStatusIn(String userId, String credentialsId, int limit, Set<CredentialsStatus> statuses) {
        List<CredentialsEvent> allEvents = findByUserIdAndCredentialsId(userId, credentialsId);
        List<CredentialsEvent> events = Lists.newArrayList();

        for (CredentialsEvent event : allEvents) {
            if (statuses.contains(event.getStatus())) {
                events.add(event);
            }
            if (events.size() >= limit) {
                break;
            }
        }

        return events;
    }
}
