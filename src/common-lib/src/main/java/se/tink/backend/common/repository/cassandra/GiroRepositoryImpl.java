package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Lists;
import java.util.Optional;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.giros.Giro;

import java.util.HashMap;
import java.util.List;

public class GiroRepositoryImpl implements GiroRepositoryCustom {
    private static final String TABLE_NAME = "giro_numbers";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification giroTableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("accountNumber", DataType.text())
                .clusteredKeyColumn("giroType", DataType.text())
                .column("created", DataType.timestamp())
                .column("name", DataType.text())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(giroTableSpec);
    }

    public Giro findOneByAccountNumberAndGiroType(String accountNumber, AccountIdentifier.Type giroType) {
        Select query = QueryBuilder.select().from(TABLE_NAME);
        query.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        query.where(QueryBuilder.eq("accountNumber", accountNumber));
        query.where(QueryBuilder.eq("giroType", giroType.toString()));

        return cassandraOperations.selectOne(query, Giro.class);
    }

    public List<Giro> findByAccountNumber(String accountNumber) {
        Select query = QueryBuilder.select().from(TABLE_NAME);
        query.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        query.setFetchSize(2);
        query.where(QueryBuilder.eq("accountNumber", accountNumber));

        return cassandraOperations.select(query, Giro.class);
    }

    public Optional<AccountIdentifier> getIdentifierFor(String accountNumber, AccountIdentifier.Type giroType) {
        AccountIdentifier identifier = null;

        Giro giro = findOneByAccountNumberAndGiroType(accountNumber, giroType);

        if (giro != null) {
            identifier = giro.toAccountIdentifier();
        }

        return Optional.ofNullable(identifier);
    }

    public List<AccountIdentifier> getIdentifiersFor(String accountNumber) {
        List<AccountIdentifier> identifiers = Lists.newArrayList();

        List<Giro> giroList = findByAccountNumber(accountNumber);

        if (giroList.size() > 0) {
            for (Giro giro : giroList) {
                identifiers.add(giro.toAccountIdentifier());
            }
        }

        return identifiers;
    }
}
