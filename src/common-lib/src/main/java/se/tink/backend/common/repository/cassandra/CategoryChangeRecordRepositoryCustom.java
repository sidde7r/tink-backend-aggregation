package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.libraries.cassandra.capabilities.Creatable;

public interface CategoryChangeRecordRepositoryCustom extends Creatable {
    void truncate();

    /**
     * Saves a lot of entities in smaller batches to not overwhelm Cassandra.
     * <p>
     * This should most certainly be handled transparently by spring-data-cassandra, but it's not. I have filed an issue about it.
     *
     * @param collect entities to save
     * @return the saved entities
     * @see https://jira.spring.io/browse/DATACASS-161
     */
    CategoryChangeRecord save(CategoryChangeRecord collect,
            long ttl, TimeUnit ttlUnit);

    Iterable<CategoryChangeRecord> saveManySafely(Iterable<CategoryChangeRecord> entities);

    Iterable<CategoryChangeRecord> saveManySafely(Iterable<CategoryChangeRecord> collect,
            long ttl, TimeUnit ttlUnit);

    void deleteByUserIdAndId(UUID userId, UUID transactionId);

    List<CategoryChangeRecord> findAllByUserIdAndTransactionId(UUID userId, UUID transactionId);

    boolean hasTtl(CategoryChangeRecord event);
}
