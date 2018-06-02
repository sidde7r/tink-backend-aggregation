package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.ExternallyDeletedTransaction;

@Deprecated
public class ExternallyDeletedTransactionRepositoryImpl implements ExternallyDeletedTransactionRepositoryCustom {
    private static final String TABLE_NAME = "externally_deleted_transactions";
    private static final int ROW_TTL_SECONDS = 60 * 60 * 24 * 14; // 14 days

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public ExternallyDeletedTransaction findByAccountIdUserIdAndExternalTransactionId(String accountId, String userId,
            String externalTransactionId) {

        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("accountid", UUIDUtils.fromTinkUUID(accountId)));
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        select.where(QueryBuilder.eq("externaltransactionid", externalTransactionId));

        return cassandraOperations.selectOne(select, ExternallyDeletedTransaction.class);
    }

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpecification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("accountid", DataType.uuid())
                .clusteredKeyColumn("externaltransactionid", DataType.text())
                .column("date", DataType.timestamp())
                .with(TableOption.COMPACTION, compactionStrategy)

                // Add time to live. This will remove data automatically.
                .with("default_time_to_live", ROW_TTL_SECONDS, false, false);

        cassandraOperations.execute(tableSpecification);
    }
}
