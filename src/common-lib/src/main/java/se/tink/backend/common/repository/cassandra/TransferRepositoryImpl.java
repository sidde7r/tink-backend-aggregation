package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.utils.guavaimpl.Predicates;

public class TransferRepositoryImpl implements TransferRepositoryCustom {

    private static final String TABLE_NAME = "transfers";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification loanDataTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.uuid())
                .column("credentialsid", DataType.uuid())
                .column("amount", DataType.cdouble())
                .column("exactamount", DataType.decimal())
                .column("currency", DataType.text())
                .column("destination", DataType.text())
                .column("originaldestination", DataType.text())
                .column("destinationmessage", DataType.text())
                .column("source", DataType.text())
                .column("originalsource", DataType.text())
                .column("sourcemessage", DataType.text())
                .column("type", DataType.text())
                .column("dueDate", DataType.timestamp())
                .column("messagetype", DataType.text())
                .column("payloadserialized", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(loanDataTableSpec);
    }

    public void deleteByUserId(String userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        cassandraOperations.execute(delete);
    }
    
    @Override
    public void deleteByUserIdAndCredentialsId(String userId, String credentialsId) {
        List<Transfer> transfers = findAllByUserIdAndCredentialsId(userId, credentialsId);
        
        for (Transfer transfer : transfers) {
            Delete delete = QueryBuilder.delete().from(TABLE_NAME);
            delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
            delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId))).and(
                    QueryBuilder.eq("id", transfer.getId()));

            cassandraOperations.executeAsynchronously(delete);
        }
    }

    @Override
    public List<Transfer> findAllByUserId(String userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        List<Transfer> transfers = cassandraOperations.select(select, Transfer.class);

        return transfers;
    }

    @Override
    public List<Transfer> findAllByUserIdAndCredentialsId(String userId, String credentialsId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));

        return FluentIterable.from(cassandraOperations.select(select, Transfer.class))
                .filter(Predicates.transferBelongsToCredentials(UUIDUtils.fromTinkUUID(credentialsId)))
                .toList();
    }

    @Override
    public Transfer findOneByUserIdAndId(String userId, String id) {
        return findOneByUserIdAndId(UUIDUtils.fromTinkUUID(userId), UUIDUtils.fromTinkUUID(id));
    }

    @Override
    public Transfer findOneByUserIdAndId(UUID userId, UUID id) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("id", id));
        Transfer transfer = cassandraOperations.selectOne(select, Transfer.class);
        return transfer;
    }

}
