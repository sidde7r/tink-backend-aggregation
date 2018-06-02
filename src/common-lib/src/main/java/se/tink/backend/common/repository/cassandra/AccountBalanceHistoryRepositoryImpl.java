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
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.AccountBalance;

public class AccountBalanceHistoryRepositoryImpl implements AccountBalanceHistoryRepositoryCustom {

    private static final String TABLE_NAME = "accounts_balance_history";
    
    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void truncate() {
        cassandraOperations.truncate(TABLE_NAME);
    }
    
    @Override
    public List<AccountBalance> findByUserId(String userId) {
        
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));

        return cassandraOperations.select(select, AccountBalance.class);
    }

    @Override
    public List<AccountBalance> findByUserIdAndAccountId(String userId, String accountId) {

        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        select.where(QueryBuilder.eq("accountid", UUIDUtils.fromTinkUUID(accountId)));

        return cassandraOperations.select(select, AccountBalance.class);
    }

    @Override
    public void deleteByUserId(String userId) {
        
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        cassandraOperations.execute(delete);
    }
    
    @Override
    public void deleteByUserIdAndAccountId(String userId, String accountId) {
        
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        delete.where(QueryBuilder.eq("accountid", UUIDUtils.fromTinkUUID(accountId)));
        cassandraOperations.execute(delete);
    }

    @Override
    public void deleteByUserIdAndAccountIdAndDate(UUID userId, UUID accountId, int date) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));
        delete.where(QueryBuilder.eq("accountid", accountId));
        delete.where(QueryBuilder.eq("date", date));

        cassandraOperations.execute(delete);
    }
    
    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpec = CreateTableSpecification
                .createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("accountid", DataType.uuid())
                .clusteredKeyColumn("date", DataType.cint())
                .column("balance", DataType.cdouble())
                .column("inserted", DataType.bigint())
                .with(TableOption.COMPACTION, compactionStrategy);
        
        cassandraOperations.execute(tableSpec);
    }
}
