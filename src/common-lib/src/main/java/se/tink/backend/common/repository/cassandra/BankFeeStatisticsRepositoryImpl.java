package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.BankFeeStatistics;

import java.util.HashMap;
import java.util.List;

public class BankFeeStatisticsRepositoryImpl implements BankFeeStatisticsRepositoryCustom {

    private static final String TABLE_NAME = "bank_fee_statistics";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification specification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("providername", DataType.text())
                .clusteredKeyColumn("year", DataType.cint())
                .clusteredKeyColumn("type", DataType.text())
                .column("averageamount", DataType.cdouble())
                .column("serializedDetails", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(specification);
    }

    @Override
    public List<BankFeeStatistics> findAllByProviderNameAndYear(String providerName, int year) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("providername", providerName));
        select.where(QueryBuilder.eq("year", year));
        return cassandraOperations.select(select, BankFeeStatistics.class);
    }


}
