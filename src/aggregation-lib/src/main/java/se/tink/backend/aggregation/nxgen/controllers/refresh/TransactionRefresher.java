package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.libraries.metrics.MetricId;

public interface TransactionRefresher extends Refresher {
    MetricId METRIC_ID = REFRESHER_METRIC_ID.label(METRIC_ITEM_TYPE, "transactions");
    List<Integer> METRIC_COUNTER_BUCKETS = ImmutableList.<Integer>builder()
            .add(0)
            .add(10)
            .add(50)
            .add(100)
            .add(500)
            .add(1000)
            .add(2000)
            .add(5000)
            .add(10000)
            .add(20000)
            .build();
    void refreshTransactions();
}
