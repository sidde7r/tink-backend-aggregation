package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.exceptions.DriverInternalError;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.CassandraTransactionDeleted;
import se.tink.libraries.uuid.UUIDUtils;

public class CassandraTransactionDeletedRepositoryImpl extends CassandraTransactionBaseRepositoryImpl
        implements CassandraTransactionDeletedRepositoryCustom {

    private static final int MAX_UPDATE_BATCH_SIZE = 30;
    private static final String TABLE_NAME = "transactions_deleted";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        createTableIfNotExist(TABLE_NAME);
    }

    @Override
    public void deleteByUserId(String userId) {
        deleteByUserId(TABLE_NAME, userId);
    }

    @Override
    public Iterable<CassandraTransactionDeleted> saveInBatches(Iterable<CassandraTransactionDeleted> entities) {
        for (List<CassandraTransactionDeleted> smallerBatch : Iterables.partition(entities, MAX_UPDATE_BATCH_SIZE)) {
            try {
                WriteOptions options = new WriteOptions();
                options.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE);

                long ttlSeconds = Duration.ofDays(60).getSeconds();
                options.setTtl((int) ttlSeconds);

                cassandraOperations.insert(smallerBatch, options);
            } catch (DriverInternalError e) {
                throw new RuntimeException(
                        "Could not save CassandraTransactionsDeleted. Possibly due to too big batch. Consider making batch smaller.",
                        e);
            }
        }
        return entities;
    }

    @Override
    public Iterable<CassandraTransactionDeleted> findByUserIdAndIds(String userId, List<String> ids) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", UUIDUtils.FROM_TINK_UUID_TRANSFORMER.apply(userId)));
        select.where(QueryBuilder.in("id", Lists.transform(ids, UUIDUtils.FROM_TINK_UUID_TRANSFORMER)));
        return cassandraOperations.select(select, CassandraTransactionDeleted.class);
    }
}
