package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.HashMap;

public class AggregatedAreaLoanDataRepositoryImpl implements AggregatedAreaLoanDataRepositoryCustom {

    private static final String TABLE_NAME = "aggregated_loans_by_area";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification aggregatedTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("areaid", DataType.uuid())
                .clusteredKeyColumn("bank", DataType.text())
                .column("avgInterest", DataType.cdouble())
                .column("avgBalance", DataType.cdouble())
                .column("bankDisplayName", DataType.text())
                .column("numLoans", DataType.bigint())
                .column("numUsers", DataType.bigint())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(aggregatedTableSpec);
    }
}
