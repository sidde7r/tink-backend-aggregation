package se.tink.backend.categorization;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.util.Collection;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.CounterCacheLoader;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.core.CategorizationCommand;

class CategorizationCounter {
    private static final MetricId CATEGORIZATION_COMMAND_METRIC_NAME = MetricId.newId("categorization_command");
    private static final MetricId CATEGORIZED_TRANSACTION_METRIC_NAME = MetricId.newId("categorized_transaction");
    private static final String CATEGORISATION_TYPE_METRIC_LABEL = "type";
    private static final String CATEGORY_SET_METRIC_LABEL = "category_set";

    private final LoadingCache<MetricId.MetricLabels, Counter> categorizationCommandCache;
    private final Counter categorizedTransactionCounter;
    private final Counter uncategorizedTransactionCounter;

    CategorizationCounter(MetricRegistry metricRegistry) {
        categorizationCommandCache = CacheBuilder.newBuilder().build(new CounterCacheLoader(
                metricRegistry, CATEGORIZATION_COMMAND_METRIC_NAME));

        categorizedTransactionCounter = metricRegistry
                .meter(CATEGORIZED_TRANSACTION_METRIC_NAME.label(CATEGORY_SET_METRIC_LABEL, "yes"));
        uncategorizedTransactionCounter = metricRegistry
                .meter(CATEGORIZED_TRANSACTION_METRIC_NAME.label(CATEGORY_SET_METRIC_LABEL, "no"));
    }

    void notCategorized() {
        uncategorizedTransactionCounter.inc();
    }

    void categorized(Collection<CategorizationCommand> categorizationCommands) {
        // Mark all type of categorizations command, which calculate most probable categoryId
        for (CategorizationCommand categorizationCommand : categorizationCommands) {
            categorizationCommandCache.getUnchecked(
                    new MetricId.MetricLabels().add(CATEGORISATION_TYPE_METRIC_LABEL, categorizationCommand.name()))
                    .inc();
        }
        categorizedTransactionCounter.inc();
    }

}
