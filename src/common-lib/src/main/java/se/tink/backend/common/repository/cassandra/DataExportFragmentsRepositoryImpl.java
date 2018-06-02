package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.cql.CqlIdentifier;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.DataExportFragment;

public class DataExportFragmentsRepositoryImpl implements DataExportFragmentsRepositoryCustom {

    private static final String TABLE_NAME = "data_export_fragments";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpec = CreateTableSpecification
                .createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("id", DataType.uuid())
                .clusteredKeyColumn(new CqlIdentifier("index", true), DataType.cint())
                .column("data", DataType.blob())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpec);
    }

    @Override
    public DataExportFragment findOneByIdAndIndex(UUID id, int index) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("id", id));
        select.where(QueryBuilder.eq(QueryBuilder.quote("index"), index));
        return cassandraOperations.selectOne(select, DataExportFragment.class);
    }

    @Override
    public void deleteByIdAndIndex(UUID id, int index) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("id", id))
                .and(QueryBuilder.eq(QueryBuilder.quote("index"), index));
        cassandraOperations.execute(delete);
    }
}
