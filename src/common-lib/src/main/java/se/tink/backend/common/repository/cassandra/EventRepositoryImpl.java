package se.tink.backend.common.repository.cassandra;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Maps;

import se.tink.backend.core.Event;

public class EventRepositoryImpl implements EventRepositoryCustom {

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification categoryChangeRecordTableSpec = CreateTableSpecification.createTable("events")
                .ifNotExists().partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.timeuuid()).column("content", DataType.text())
                .column("date", DataType.timestamp()).column("type", DataType.text())
                .column("oldid", DataType.bigint())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);
        cassandraOperations.execute(categoryChangeRecordTableSpec);
    }

    @Override
    public List<Event> findLastByUserId(UUID userId, int limit) {
        Select select = QueryBuilder.select().all().from("events");
        select.where(QueryBuilder.eq("userid", userId));

        // We have users with over 250000 events added to them. Without this we will receive read timeouts when fetching
        // all the events. Note that we will still be loading all the users events into memory!
        select.setFetchSize(500);
        
        select.orderBy(QueryBuilder.desc("id"));
        select.limit(limit);
        
        return cassandraOperations.select(select, Event.class);
    }

    @Override
    public List<Event> findUserEventsAfter(UUID userId, Date date) {
        Select select = QueryBuilder.select().all().from("events");

        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.gt("id", UUIDs.endOf(date.getTime())));

        select.setFetchSize(500);

        return cassandraOperations.select(select, Event.class);
    }

}
