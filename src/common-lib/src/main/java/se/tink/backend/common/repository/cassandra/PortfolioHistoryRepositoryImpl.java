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
import se.tink.backend.core.PortfolioHistory;

public class PortfolioHistoryRepositoryImpl implements PortfolioHistoryRepositoryCustom {

    private static final String TABLE_NAME = "portfolio_history";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification portfolioHistoryTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("accountid", DataType.uuid())
                .clusteredKeyColumn("portfolioid", DataType.uuid())
                .clusteredKeyColumn("timestamp", DataType.timestamp())
                .column("totalvalue", DataType.cdouble())
                .column("totalprofit", DataType.cdouble())
                .column("cashvalue", DataType.cdouble())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(portfolioHistoryTableSpec);
    }

    @Override
    public List<PortfolioHistory> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));

        return cassandraOperations.select(select, PortfolioHistory.class);
    }

    @Override
    public List<PortfolioHistory> findAllByUserIdAndAccountId(UUID userId, UUID accountId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("accountid", accountId));

        return cassandraOperations.select(select, PortfolioHistory.class);
    }

    @Override
    public List<PortfolioHistory> findAllByUserIdAndAccountIdAndPortfolioId(UUID userId, UUID accountId,
            UUID portfolioId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("accountid", accountId));
        select.where(QueryBuilder.eq("portfolioid", portfolioId));

        return cassandraOperations.select(select, PortfolioHistory.class);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));

        cassandraOperations.execute(delete);
    }

    @Override
    public void deleteByUserIdAndAccountId(UUID userId, UUID accountId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));
        delete.where(QueryBuilder.eq("accountid", accountId));

        cassandraOperations.execute(delete);
    }
}
