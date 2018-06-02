package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.UserProfileData;

public class UserProfileDataRepositoryImpl implements UserProfileDataRepositoryCustom {

    private static final String TABLE_NAME = "users_profile_data";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification spec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("name", DataType.text())
                .column("updated", DataType.timestamp())
                .column("value", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(spec);
    }

    @Override
    public List<UserProfileData> findAllByUserId(String userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        return cassandraOperations.select(select, UserProfileData.class);
    }
    
    @Override
    public void deleteByUserId(String userId) {
        deleteByUserId(UUIDUtils.fromTinkUUID(userId));
    }

    public void deleteByUserId(UUID userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", userId));
        cassandraOperations.execute(delete);
    }

    @Override
    public ImmutableMap<String, UserProfileData> getValuesByNameForUserId(String userId) {
        return Maps.uniqueIndex(findAllByUserId(userId), UserProfileData::getName);
    }
}
