package se.tink.backend.common.repository.cassandra;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;

public class ProviderTriesRepositoryImpl implements ProviderTriesRepositoryCustom {
    private static final String TABLE_NAME = "providers_tries";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification categoryChangeRecordTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists().partitionKeyColumn("providername", DataType.text())
                .column("onetryfailure", DataType.cint())
                .column("twotriesfailure", DataType.cint())
                .column("threetriesfailure", DataType.cint())
                .column("tries", DataType.cint())

                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);
        cassandraOperations.execute(categoryChangeRecordTableSpec);
    }

    @Override
    public void truncate() {
        cassandraOperations.truncate(TABLE_NAME);
    }
}
