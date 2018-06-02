package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
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
import se.tink.backend.core.product.ProductInstance;

public class ProductInstanceRepositoryImpl implements ProductInstanceRepositoryCustom {

    private static final String TABLE_NAME = "products_instances";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpecification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("id", DataType.uuid())
                .column("filterid", DataType.uuid())
                .column("properties", DataType.text())
                .column("templateid", DataType.uuid())
                .column("validfrom", DataType.timestamp())
                .column("validto", DataType.timestamp())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpecification);
    }

    @Override
    public List<ProductInstance> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", userId));
        return cassandraOperations.select(select, ProductInstance.class);
    }
    
    @Override
    public ProductInstance findByUserIdAndId(UUID userId, UUID id) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("id", id));
        return cassandraOperations.selectOne(select, ProductInstance.class);
    }
}
