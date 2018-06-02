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
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.LoanDetails;
import se.tink.backend.utils.LogUtils;

public class LoanDetailsRepositoryImpl implements LoanDetailsRepositoryCustom {
    private static final String TABLE_NAME = "loan_details";
    private static final LogUtils log = new LogUtils(LoanDetailsRepository.class);

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification loanDataTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("accountid", DataType.uuid())
                .column("coapplicant", DataType.cboolean())
                .column("applicants", DataType.text())
                .column("loansecurity", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(loanDataTableSpec);
    }

    @Override
    public void deleteByAccountId(UUID accountId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("accountid", accountId));
        cassandraOperations.execute(delete);
    }

    @Override
    public LoanDetails findOneByAccountId(UUID accountId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("accountid", accountId));

        return cassandraOperations.selectOne(select, LoanDetails.class);
    }

    @Override
    public boolean hasBeenUpdated(LoanDetails loanDetails) {
        LoanDetails existingDetails = findOneByAccountId(loanDetails.getAccountId());

        return existingDetails == null || loanDetails.hasUpdatedSince(existingDetails);
    }
}
