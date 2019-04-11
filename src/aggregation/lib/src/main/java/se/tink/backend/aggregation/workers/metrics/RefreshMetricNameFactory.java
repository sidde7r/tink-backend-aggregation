package se.tink.backend.aggregation.workers.metrics;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.List;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.metrics.utils.MetricsUtils;

public class RefreshMetricNameFactory {

    private static Function<RefreshableItem, String> ITEM_TO_METRIC_NAME =
            item -> {
                if (RefreshableItem.TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS.equals(item)) {
                    return "transactions";
                }
                return MetricsUtils.cleanMetricName(item.toString().toLowerCase())
                        .replaceAll("_", "");
            };

    public static String createCleanName(RefreshableItem item) {
        return ITEM_TO_METRIC_NAME.apply(item);
    }

    public static String createOperationName(List<RefreshableItem> items, boolean isManual) {
        List<String> names = Lists.newArrayList();

        if (items.size() < RefreshableItem.values().length) {
            names.addAll(
                    FluentIterable.from(items)
                            .filter(Predicates.notNull())
                            .transform(ITEM_TO_METRIC_NAME)
                            .toSortedList(Ordering.natural()));
        }

        // prepend name with refresh and append with manual/auto
        names.add(0, "refresh");
        names.add(isManual ? "manual" : "auto");

        return Joiner.on('-').join(names);
    }
}
