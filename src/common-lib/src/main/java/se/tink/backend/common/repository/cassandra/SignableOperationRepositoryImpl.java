package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.enums.SignableOperationTypes;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.utils.guavaimpl.Predicates;

public class SignableOperationRepositoryImpl implements SignableOperationRepositoryCustom {

    private static final String TABLE_NAME = "signable_operations";

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
                .column("underlyingid", DataType.uuid())
                .column("credentialsid", DataType.uuid())
                .column("type", DataType.text())
                .column("status", DataType.text())
                .column("statusmessage", DataType.text())
                .column("created", DataType.timestamp())
                .column("updated", DataType.timestamp())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(loanDataTableSpec);
    }

    @Override
    public void deleteByUserId(String userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        cassandraOperations.execute(delete);
    }

    @Override
    public Optional<SignableOperation> findOneByUserIdAndUnderlyingId(String userId, UUID underlyingId) {
        // This can be improved by streaming or paging through a user's SignableOperations and returning
        // when the underlyingId has been found
        List<SignableOperation> operations = findAllByUserId(userId);
        return operations.stream().filter(o -> Predicates.signableOperationsWithUnderlyingId(underlyingId).apply(o))
                .findFirst();
    }

    @Override
    public List<SignableOperation> findAllByUserId(String userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        return cassandraOperations.select(select, SignableOperation.class);
    }

    @Override
    public List<SignableOperation> findAllByUserIdAndType(String userId, final SignableOperationTypes type) {
        return Lists.newArrayList(Iterables.filter(findAllByUserId(userId), input -> input.getType() == type));
    }

}
