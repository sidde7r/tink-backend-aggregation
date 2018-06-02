package se.tink.backend.common.repository.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.cassandra.core.keyspace.TableOption;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.application.ApplicationArchiveRow;

public class ApplicationArchiveRepositoryImpl implements ApplicationArchiveRepositoryCustom {
    private static final String TABLE_NAME = "applications_archive";

    @Autowired
    private CassandraOperations cassandraOperations;

    @Override
    public void createTableIfNotExist() {
        HashMap<Object, Object> compactionStrategy = Maps.newHashMap();
        compactionStrategy.put(TableOption.CompactionOption.CLASS, "LeveledCompactionStrategy");

        CreateTableSpecification tableSpec = CreateTableSpecification.createTable(TABLE_NAME)
                .ifNotExists()
                .partitionKeyColumn("userid", DataType.uuid())
                .clusteredKeyColumn("applicationid", DataType.uuid())
                .column("applicationtype", DataType.text())
                .column("status", DataType.text())
                .column("content", DataType.text())
                .column("externalid", DataType.text())
                .column("notes", DataType.text())
                .column("timestamp", DataType.timestamp())
                // Uses less space at the expense of slightly higher write latency.
                .with(TableOption.COMPACTION, compactionStrategy);

        cassandraOperations.execute(tableSpec);
    }

    @Override
    public void deleteByUserId(String userId) {
        // Nothing since we aren't allowed to delete our archived user data for applications
    }

    @Override
    public List<ApplicationArchiveRow> findAllByUserId(UUID userId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.where(QueryBuilder.eq("userid", userId));

        return cassandraOperations.select(select, ApplicationArchiveRow.class);
    }

    private ApplicationArchiveRow ensureOneByUserIdAndApplicationId(UUID userId, UUID applicationId) {
        Optional<ApplicationArchiveRow> archiveRowOptional = findByUserIdAndApplicationId(userId, applicationId);

        if (!archiveRowOptional.isPresent()) {
            throw new IllegalStateException("No application row archived yet, there should be one");
        }

        return archiveRowOptional.get();
    }

    private void updateRow(ApplicationArchiveRow archiveRow) {
        WriteOptions writeOptions = new WriteOptions();
        writeOptions.setConsistencyLevel(org.springframework.cassandra.core.ConsistencyLevel.LOCAL_QUOROM);
        cassandraOperations.update(archiveRow, writeOptions);
    }

    @Override
    public void setToSigned(UUID userId, UUID applicationId, String externalId) {
        setToSignedWithAddedNotes(userId, applicationId, externalId, null);
    }

    @Override
    public void setToSignedWithAddedNotes(UUID userId, UUID applicationId, String externalId, String notes) {
        ApplicationArchiveRow archiveRow = ensureOneByUserIdAndApplicationId(userId, applicationId);

        if (Objects.equals(archiveRow.getStatus(), ApplicationArchiveRow.Status.SIGNED)) {
            if (Objects.equals(archiveRow.getExternalId(), externalId)) {
                // The archived application already has the target state. Abort nicely.
                return;
            }

            throw new IllegalStateException(String.format(
                    "[userId:%s, applicationId:%s] The application has already been signed.",
                    UUIDUtils.toTinkUUID(userId), UUIDUtils.toTinkUUID(applicationId)));
        }

        // Set external id and new status.
        archiveRow.setExternalId(externalId);
        archiveRow.setStatus(ApplicationArchiveRow.Status.SIGNED);

        // Add notes if any
        if (!Strings.isNullOrEmpty(notes)) {
            // Also preserve any previous notes for the row
            String existingNotes = archiveRow.getNotes();
            archiveRow.setNotes(
                    !Strings.isNullOrEmpty(existingNotes) ?
                            existingNotes + "; " + notes :
                            notes);
        }

        updateRow(archiveRow);
    }

    @Override
    public Optional<ApplicationArchiveRow> findByUserIdAndApplicationId(UUID userId, UUID applicationId) {
        Select select = QueryBuilder.select().from(TABLE_NAME);
        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        select.setFetchSize(1);
        select.where(QueryBuilder.eq("userid", userId));
        select.where(QueryBuilder.eq("applicationid", applicationId));

        List<ApplicationArchiveRow> rows = cassandraOperations
                .select(select, ApplicationArchiveRow.class);

        // This repos PK is userId + applicationId, so it's guaranteed that repo contains at max 1 result row
        if (rows.size() > 0) {
            return Optional.of(rows.get(0));
        } else {
            return Optional.empty();
        }
    }
}
