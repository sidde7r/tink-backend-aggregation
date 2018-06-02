package se.tink.backend.consent.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;

public class ConsentRepositoryImpl implements ConsentRepositoryCustom {
    private static final String TABLE_NAME = "consents";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        CreateTableSpecification tableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("key", DataType.text())
                .clusteredKeyColumn("version", DataType.text())
                .clusteredKeyColumn("locale", DataType.text())
                .column("title", DataType.text())
                .column("body", DataType.text())
                .column("messages", DataType.frozenList(DataType.text()))
                .column("attachments", DataType.frozenMap(DataType.text(), DataType.text()))
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpec);
    }
}
