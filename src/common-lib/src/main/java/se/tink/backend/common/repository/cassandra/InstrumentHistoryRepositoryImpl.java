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
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.InstrumentHistory;

public class InstrumentHistoryRepositoryImpl implements InstrumentHistoryRepositoryCustom {

    private static final String TABLE_NAME = "instrument_history";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification instrumentHistoryTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("portfolioid", DataType.uuid())
                .clusteredKeyColumn("instrumentid", DataType.uuid())
                .clusteredKeyColumn("timestamp", DataType.timestamp())
                .column("quantity", DataType.cdouble())
                .column("averageacquisitionprice", DataType.cdouble())
                .column("profit", DataType.cdouble())
                .column("marketvalue", DataType.cdouble())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(instrumentHistoryTableSpec);
    }

    @Override
    public List<InstrumentHistory> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        return cassandraOperations.select(select, InstrumentHistory.class);
    }

    @Override
    public List<InstrumentHistory> findAllByUserIdAndPortfolioId(UUID userId, UUID portfolioId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("portfolioid", portfolioId));

        return cassandraOperations.select(select, InstrumentHistory.class);
    }

    @Override
    public List<InstrumentHistory> findAllByUserIdAndPortfolioIdAndInstrumentId(UUID userId, UUID portfolioId,
            UUID instrumentId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("portfolioid", portfolioId));
        select.where(QueryBuilder.eq("instrumentid", instrumentId));

        return cassandraOperations.select(select, InstrumentHistory.class);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));

        cassandraOperations.execute(delete);
    }

    @Override
    public void deleteByUserIdAndPortfolioId(UUID userId, UUID portfolioId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));
        delete.where(QueryBuilder.eq("portfolioid", portfolioId));

        cassandraOperations.execute(delete);
    }

    @Override
    public void deleteByUserIdAndPortfolioIdAndInstrumentId(UUID userId, UUID portfolioId, UUID instrumentId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));
        delete.where(QueryBuilder.eq("portfolioid", portfolioId));
        delete.where(QueryBuilder.eq("instrumentid", instrumentId));

        cassandraOperations.execute(delete);
    }
}
