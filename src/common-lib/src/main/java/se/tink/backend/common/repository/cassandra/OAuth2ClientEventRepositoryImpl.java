package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.OAuth2ClientEvent;

public class OAuth2ClientEventRepositoryImpl implements OAuth2ClientEventRepositoryCustom {

    private static final String TABLE_NAME = "oauth2_client_events";

    @Autowired
    private CassandraOperations cassandraOperations;
    
    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification categoryChangeRecordTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("clientid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.timeuuid(), Ordering.DESCENDING)
                .column("type", DataType.text())
                .column("payload", DataType.text())
                .column("timestamp", DataType.timestamp())
                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(categoryChangeRecordTableSpec);
    }

    @Override
    public List<OAuth2ClientEvent> findAllByClientIdAndDateBetween(UUID clientId, Date min, Date max) {
        final UUID lower = UUIDs.startOf(min.getTime());
        final UUID upper = UUIDs.endOf(max.getTime());

        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("clientid", clientId));
        select.where(QueryBuilder.gt("id", lower));
        select.where(QueryBuilder.lt("id", upper));

        return cassandraOperations.select(select, OAuth2ClientEvent.class);
    }
}
