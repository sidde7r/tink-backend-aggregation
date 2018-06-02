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
import se.tink.backend.core.Instrument;

public class InstrumentRepositoryImpl implements InstrumentRepositoryCustom {

    private static final String TABLE_NAME = "instruments";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification instrumentsTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("portfolioid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.uuid())
                .column("isin", DataType.text())
                .column("marketplace", DataType.text())
                .column("averageacquisitionprice", DataType.cdouble())
                .column("brokerage", DataType.cdouble())
                .column("currency", DataType.text())
                .column("marketvalue", DataType.cdouble())
                .column("name", DataType.text())
                .column("price", DataType.cdouble())
                .column("quantity", DataType.cdouble())
                .column("profit", DataType.cdouble())
                .column("ticker", DataType.text())
                .column("type", DataType.text())
                .column("rawtype", DataType.text())
                .column("uniqueidentifier", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(instrumentsTableSpec);
    }

    @Override
    public List<Instrument> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));

        return cassandraOperations.select(select, Instrument.class);
    }

    @Override
    public List<Instrument> findAllByUserIdAndPortfolioId(UUID userId, UUID portfolioId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("portfolioid", portfolioId));

        return cassandraOperations.select(select, Instrument.class);
    }

    @Override
    public Instrument findOneByUserIdAndPortfolioIdAndInstrumentId(UUID userId, UUID portfolioId, UUID instrumentId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("portfolioid", portfolioId));
        select.where(QueryBuilder.eq("id", instrumentId));
        return cassandraOperations.selectOne(select, Instrument.class);
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
    public void deleteByUserIdAndPortfolioIdAndId(UUID userId, UUID portfolioId, UUID id) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));
        delete.where(QueryBuilder.eq("portfolioid", portfolioId));
        delete.where(QueryBuilder.eq("id", id));

        cassandraOperations.execute(delete);
    }
}
