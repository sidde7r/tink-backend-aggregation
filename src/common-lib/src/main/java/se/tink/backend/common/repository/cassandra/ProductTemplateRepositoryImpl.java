package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.product.ProductTemplate;

public class ProductTemplateRepositoryImpl implements ProductTemplateRepositoryCustom {

    private static final String TABLE_NAME = "products_templates";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpecification = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("id", DataType.uuid())
                .column("name", DataType.text())
                .column("properties", DataType.text())
                .column("providername", DataType.text())
                .column("status", DataType.text())
                .column("type", DataType.text())                
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpecification);
    }

    @Override
    public ProductTemplate findById(UUID id) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("id", id));
        return cassandraOperations.selectOne(select, ProductTemplate.class);
    }
}
