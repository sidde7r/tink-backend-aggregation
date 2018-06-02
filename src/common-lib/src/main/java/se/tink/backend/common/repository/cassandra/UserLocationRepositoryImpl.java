package se.tink.backend.common.repository.cassandra;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;

import se.tink.backend.core.UserLocation;
import se.tink.libraries.uuid.UUIDUtils;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Maps;

public class UserLocationRepositoryImpl implements UserLocationRepositoryCustom {
    
    private static final String TABLE_NAME = "users_locations";
    
    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpecification = CreateTableSpecification.createTable("users_locations")
                .ifNotExists().partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.timeuuid()).column("accuracy", DataType.decimal())
                .column("date", DataType.timestamp()).column("latitude", DataType.decimal())
                .column("longitude", DataType.decimal())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);
        cassandraOperations.execute(tableSpecification);
    }

    @Override
    public void deleteByUserId(String userId) {
        Delete delete = QueryBuilder.delete().from("users_locations");

        delete.where(QueryBuilder.eq("userId", UUIDUtils.fromTinkUUID(userId)));
        delete.setConsistencyLevel(ConsistencyLevel.QUORUM);

        cassandraOperations.execute(delete);
    }

    @Override
    public List<UserLocation> findAllByUserId(String userId) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));

        select.setConsistencyLevel(ConsistencyLevel.ONE);

        return cassandraOperations.select(select, UserLocation.class);
    }

    @Override
    public List<UserLocation> findAllByUserIdAndDateBetween(String userId, Date from, Date to) {
        Select select = QueryBuilder.select().all().from(TABLE_NAME);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)))
            .and(QueryBuilder.gt("id", UUIDs.startOf(from.getTime())))
            .and(QueryBuilder.lt("id", UUIDs.endOf(to.getTime())));

        select.setConsistencyLevel(ConsistencyLevel.ONE);

        return cassandraOperations.select(select, UserLocation.class);
    }
}
