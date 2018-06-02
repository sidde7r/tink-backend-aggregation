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
import se.tink.backend.core.Portfolio;

public class PortfolioRepositoryImpl implements PortfolioRepositoryCustom {

    private static final String TABLE_NAME = "portfolios";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification portfolioTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("accountid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.uuid())
                .column("uniqueidentifier", DataType.text())
                .column("totalvalue", DataType.cdouble())
                .column("totalprofit", DataType.cdouble())
                .column("cashvalue", DataType.cdouble())
                .column("type", DataType.text())
                .column("rawtype", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(portfolioTableSpec);
    }

    @Override
    public List<Portfolio> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));

        return cassandraOperations.select(select, Portfolio.class);
    }

    @Override
    public List<Portfolio> findAllByUserIdAndAccountId(UUID userId, UUID accountId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("accountid", accountId));

        return cassandraOperations.select(select, Portfolio.class);
    }

    @Override
    public Portfolio findOneByUserIdAndAccountIdAndPortfolioId(UUID userId, UUID accountId, UUID portfolioId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.ONE);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("accountid", accountId));
        select.where(QueryBuilder.eq("id", portfolioId));

        return cassandraOperations.selectOne(select, Portfolio.class);
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
