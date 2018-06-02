package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.TransactionExternalId;
import se.tink.libraries.uuid.UUIDUtils;

public class TransactionExternalIdRepositoryImpl implements TransactionExternalIdRepositoryCustom {
    private static final String TABLE_NAME = "transactions_external_id";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public TransactionExternalId findByAccountIdUserIdAndExternalTransactionId(String accountId, String userId,
            String externalTransactionId) {

        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("accountid", UUIDUtils.fromTinkUUID(accountId)));
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        select.where(QueryBuilder.eq("externaltransactionid", externalTransactionId));

        return cassandraOperations.selectOne(select, TransactionExternalId.class);
    }

    @Override
    public List<TransactionExternalId> findAllByAccountIdUserIdAndExternalTransactionIds(String accountId,
            String userId, List<String> externalTransactionIds) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("accountid", UUIDUtils.fromTinkUUID(accountId)));
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        select.where(QueryBuilder.in("externaltransactionid", externalTransactionIds));

        List<TransactionExternalId> selected = cassandraOperations.select(select, TransactionExternalId.class);
        return selected == null ? Collections.emptyList() : selected;
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
                .column("deleted", DataType.cboolean())
                .column("transactionid", DataType.uuid())
                .column("updated", DataType.timestamp())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpecification);
    }
}
