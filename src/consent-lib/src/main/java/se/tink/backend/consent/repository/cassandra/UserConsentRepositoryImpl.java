package se.tink.backend.consent.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;

public class UserConsentRepositoryImpl implements UserConsentRepositoryCustom {
    private static final String TABLE_NAME = "users_consents";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        CreateTableSpecification tableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userId", DataType.uuid())
                .clusteredKeyColumn("id", DataType.uuid())
                .column("key", DataType.text())
                .column("version", DataType.text())
                .column("locale", DataType.text())
                .column("username", DataType.text())
                .column("action", DataType.text())
                .column("payload", DataType.blob())
                .column("timestamp", DataType.timestamp())
                .column("signature", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpec);
    }
}
