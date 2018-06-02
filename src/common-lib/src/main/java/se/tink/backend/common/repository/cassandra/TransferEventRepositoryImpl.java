package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.libraries.uuid.UUIDUtils;

public class TransferEventRepositoryImpl implements TransferEventRepositoryCustom {

    private static final String TABLE_NAME = "transfers_events";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        HashMap<Object, Object> compression = Maps.newHashMap();
        compression.put(TableOption.CompressionOption.SSTABLE_COMPRESSION, "DeflateCompressor");

        final CreateTableSpecification specification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("transferid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.timeuuid())
                .column("remoteaddress", DataType.text())
                .column("credentialsid", DataType.uuid())
                .column("amount", DataType.cdouble())
                .column("currency", DataType.text())
                .column("created", DataType.timestamp())
                .column("updated", DataType.timestamp())
                .column("destination", DataType.text())
                .column("originaldestination", DataType.text())
                .column("destinationmessage", DataType.text())
                .column("source", DataType.text())
                .column("originalsource", DataType.text())
                .column("sourcemessage", DataType.text())
                .column("messagetype", DataType.text())
                .column("status", DataType.text())
                .column("statusmessage", DataType.text())
                .column("eventsource", DataType.text())
                .column("transfertype", DataType.text())
                .with(TableOption.COMPRESSION, compression)
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(specification);
    }

    @Override
    public List<TransferEvent> findAllByUserIdAndTransferId(UUID userId, UUID transferId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("transferid", transferId));
        return cassandraOperations.select(select, TransferEvent.class);
    }

    @Override public List<TransferEvent> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", userId));
        return cassandraOperations.select(select, TransferEvent.class);
    }

    @Override
    public void deleteByUserId(String userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        cassandraOperations.execute(delete);
    }

}
