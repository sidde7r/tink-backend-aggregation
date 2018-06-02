package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.core.UserCoordinates;

public class UserCoordinatesRepositoryImpl implements UserCoordinatesRepositoryCustom {

    private static final String TABLE_NAME = "users_coordinates";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification aggregatedTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .column("address", DataType.text())
                .column("latitude", DataType.cdouble())
                .column("longitude", DataType.cdouble())
                .column("areaid", DataType.uuid())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(aggregatedTableSpec);
    }

    @Override
    public UserCoordinates findOneByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setFetchSize(1);
        select.where(QueryBuilder.eq("userid", userId));

        return cassandraOperations.selectOne(select, UserCoordinates.class);
    }

    @Override
    public ConcurrentHashMap<String, UserCoordinates> findAllCoordinatesByAddress() {
        List<UserCoordinates> all = cassandraOperations.selectAll(UserCoordinates.class);

        ConcurrentHashMap<String, UserCoordinates> map = new ConcurrentHashMap<>();
        for (UserCoordinates userCoordinates : all) {
            map.put(userCoordinates.getAddress(), userCoordinates);
        }

        return map;
    }
}
