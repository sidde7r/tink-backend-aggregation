package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.account.TransferDestinationPattern;
import se.tink.libraries.uuid.UUIDUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TransferDestinationPatternRepositoryImpl implements TransferDestinationPatternRepositoryCustom {

    private static final String TABLE_NAME = "transfer_destination_patterns";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification spec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("accountid", DataType.uuid())
                .clusteredKeyColumn("type", DataType.text())
                .clusteredKeyColumn("pattern", DataType.text())
                .column("name", DataType.text())
                .column("bank", DataType.text())
                .column("matchesmultiple", DataType.cboolean())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(spec);
    }

    @Override
    public void deleteByUserId(String userId) {
        deleteByUserId(UUIDUtils.fromTinkUUID(userId));
    }

    @Override
    public void deleteByUserId(UUID userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));
        cassandraOperations.execute(delete);
    }

    @Override
    public void deleteByUserIdAndAccountId(String userId, String accountId) {
        deleteByUserIdAndAccountId(UUIDUtils.fromTinkUUID(userId), UUIDUtils.fromTinkUUID(accountId));
    }

    @Override
    public void deleteByUserIdAndAccountId(UUID userId, UUID accountId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));
        delete.where(QueryBuilder.eq("accountid", accountId));
        cassandraOperations.execute(delete);
    }

    @Override
    public ListMultimap<String, TransferDestinationPattern> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", userId));
        List<TransferDestinationPattern> all = cassandraOperations.select(select, TransferDestinationPattern.class);

        return Multimaps.index(all, new Function<TransferDestinationPattern, String>() {

            @Nullable
            @Override
            public String apply(TransferDestinationPattern p) {
                return UUIDUtils.toTinkUUID(p.getAccountId());
            }
        });
    }

    @Override
    public List<TransferDestinationPattern> findAllByUserIdAndAccountId(String userId, String accountId) {
        return findAllByUserIdAndAccountId(UUIDUtils.fromTinkUUID(userId), UUIDUtils.fromTinkUUID(accountId));
    }

    @Override
    public List<TransferDestinationPattern> findAllByUserIdAndAccountId(UUID userId, UUID accountId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("accountid", accountId));
        return cassandraOperations.select(select, TransferDestinationPattern.class);
    }
}
