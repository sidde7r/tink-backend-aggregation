package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.cql.CqlIdentifier;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.cassandra.core.keyspace.TableOption.CompactionOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.CompressedDocument;

/**
 * NOTICE:
 * With the current set-up (storing documents to C*) we have a hard limit of about ~100MB per partition key.
 * The documents table is now set up with only userId as the partition key and hence we should not store
 * more than a few documents per user. As of initial development of this document storage, the only use-case
 * stores a maximum of one document per user. If the document database will be used for further use-cases,
 * the solution might have to be re-thought.
 */
public class DocumentRepositoryImpl implements DocumentRepositoryCustom {

    private static final String TABLE_NAME = "documents";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(CompactionOption.CLASS, "LeveledCompactionStrategy");

        final CreateTableSpecification tableSpec = CreateTableSpecification
                .createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn(new CqlIdentifier("token", true), DataType.uuid())
                .column("identifier", DataType.text())
                .column("mimetype", DataType.text())
                .column("compresseddocument", DataType.blob())
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpec);
    }

    @Override
    public Optional<CompressedDocument> findOneByUserIdAndIdentifier(UUID userId, String identifier) {

        return findAllByUserId(userId).stream()
                .filter(d -> getDocumentByIdentifier(identifier).apply(d))
                .findFirst();
    }

    public List<CompressedDocument> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1000);
        select.where(QueryBuilder.eq("userid", userId));
        return cassandraOperations.select(select, CompressedDocument.class);
    }

    @Override
    public CompressedDocument findOneByUserIdAndToken(UUID userId, UUID token) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq(QueryBuilder.quote("token"), token));
        return cassandraOperations.selectOne(select, CompressedDocument.class);
    }

    @Override
    public void deleteByUserId(String userId) {
        Delete delete = QueryBuilder.delete().from(TABLE_NAME);
        delete.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        delete.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(userId)));
        cassandraOperations.execute(delete);
    }

    private Predicate<CompressedDocument> getDocumentByIdentifier(final String identifier) {
        return doc -> Objects.equals(identifier, doc.getIdentifier());
    }
}
