package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.exceptions.DriverInternalError;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.merchants.MerchantWizardSkippedTransaction;
import se.tink.libraries.uuid.UUIDUtils;

import java.util.HashMap;
import java.util.List;

public class MerchantWizardSkippedTransactionRepositoryImpl
        implements MerchantWizardSkippedTransactionRepositoryCustom {

    private static final int MAX_UPDATE_BATCH_SIZE = 20;
    private static final String TABLE_NAME = "merchant_wizard_skipped_transactions";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void deleteByUserId(String userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        cassandraOperations.execute(delete);
    }

    @Override
    public void createTableIfNotExist() {

        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpec = CreateTableSpecification
                .createTable(TABLE_NAME).ifNotExists()
                .partitionKeyColumn("userId", DataType.uuid())
                .clusteredKeyColumn("transactionId", DataType.uuid())
                .column("inserted", DataType.timestamp())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpec);
    }

    @Override
    public Iterable<MerchantWizardSkippedTransaction> saveInBatches(
            Iterable<MerchantWizardSkippedTransaction> entities) {
        for (List<MerchantWizardSkippedTransaction> smallerBatch : Iterables
                .partition(entities, MAX_UPDATE_BATCH_SIZE)) {
            try {
                WriteOptions options = new WriteOptions();
                options.setConsistencyLevel(org.springframework.cassandra.core.ConsistencyLevel.LOCAL_QUOROM);
                cassandraOperations.insert(smallerBatch, options);
            } catch (DriverInternalError e) {
                throw new RuntimeException("Could not save CassandraMerchantWizardSkippedTransaction.", e);
            }
        }
        return entities;
    }

    @Override
    public List<MerchantWizardSkippedTransaction> findAllByUserId(String userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        return cassandraOperations.select(select, MerchantWizardSkippedTransaction.class);
    }
}
