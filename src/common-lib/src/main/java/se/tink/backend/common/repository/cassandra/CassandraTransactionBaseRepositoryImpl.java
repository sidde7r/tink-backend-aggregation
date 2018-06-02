package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraTransactionBaseRepositoryImpl {

    @Autowired
    private CassandraOperations cassandraOperations;

    public void deleteByUserId(String tableName, String userId) {
        Delete delete = QueryBuilder.delete().from(tableName);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        cassandraOperations.execute(delete);
    }
    
    public void createTableIfNotExist(String tableName) {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification transactionTableSpec = CreateTableSpecification
                .createTable(tableName)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.uuid())
                .column("accountid", DataType.uuid())
                .column("exactamount", DataType.decimal())
                .column("categoryId", DataType.uuid())
                .column("categoryType", DataType.text())
                .column("credentialsId", DataType.uuid())
                .column("date", DataType.timestamp())
                .column("description", DataType.text())
                .column("formatteddescription", DataType.text())
                .column("inserted", DataType.bigint())
                .column("internalpayloadserialized", DataType.text())
                .column("lastmodified", DataType.timestamp())
                .column("merchantId", DataType.uuid())
                .column("notes", DataType.text())
                .column("exactoriginalamount", DataType.decimal())
                .column("originaldate", DataType.timestamp())
                .column("originaldescription", DataType.text())
                .column("partsserialized", DataType.text())
                .column("payloadserialized", DataType.text())
                .column("pending", DataType.cboolean())
                .column("timestamp", DataType.bigint())
                .column("type", DataType.text())
                .column("usermodifiedamount", DataType.cboolean())
                .column("usermodifiedcategory", DataType.cboolean())
                .column("usermodifieddate", DataType.cboolean())
                .column("usermodifieddescription", DataType.cboolean())
                .column("usermodifiedlocation", DataType.cboolean())
                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);

        if (Objects.equal(tableName, "transactions_deleted")) {
            transactionTableSpec.column("amount", DataType.cdouble());
            transactionTableSpec.column("originalamount", DataType.cdouble());
            transactionTableSpec.column("deleted", DataType.timestamp());
        }

        cassandraOperations.execute(transactionTableSpec);
    }
}
