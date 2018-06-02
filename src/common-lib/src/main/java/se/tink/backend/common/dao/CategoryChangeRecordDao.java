package se.tink.backend.common.dao;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;

public class CategoryChangeRecordDao {
    public static final int CATEGORY_CHANGE_RECORD_TTL_DAYS = 4 * 4 * 7; // ~4 months
    private static final MetricId QUERY_RECORDS_TIMER_METRIC_NAME = MetricId
            .newId("query_category_change_records");
    private static final MetricId SAVE_RECORDS_TIMER_METRIC_NAME = MetricId
            .newId("persist_category_change_records");
    private final CategoryChangeRecordRepository repository;

    private final Timer queryTimer;
    private final Timer saveTimer;

    // TODO: Make private. See https://github.com/google/guice/wiki/KeepConstructorsHidden.
    @Inject
    public CategoryChangeRecordDao(CategoryChangeRecordRepository repository, MetricRegistry registry) {
        this.repository = repository;

        this.queryTimer = registry.timer(QUERY_RECORDS_TIMER_METRIC_NAME);
        this.saveTimer = registry.timer(SAVE_RECORDS_TIMER_METRIC_NAME);
    }

    public void save(CategoryChangeRecord record) {
        // TTL = 0 means no TTL.
        save(record, 0, TimeUnit.SECONDS);
    }

    public void save(CategoryChangeRecord record, long ttl, TimeUnit ttlUnit) {
        repository.save(record, ttl, ttlUnit);
    }

    public void save(Collection<CategoryChangeRecord> records) {
        final Context timer = saveTimer.time();
        try {
            repository.saveManySafely(records);
        } finally {
            timer.stop();
        }
    }

    public void save(Collection<CategoryChangeRecord> records, long ttl, TimeUnit ttlUnit) {
        repository.saveManySafely(records, ttl, ttlUnit);
    }

    public void deleteByUserIdAndId(UUID userId, UUID id) {
        repository.deleteByUserIdAndId(userId, id);
    }

    public List<CategoryChangeRecord> findAllByUserIdAndId(UUID userId, UUID transactionId) {
        Context timer = queryTimer.time();
        try {
            return repository.findAllByUserIdAndTransactionId(userId, transactionId);
        } finally {
            timer.stop();
        }
    }
}
