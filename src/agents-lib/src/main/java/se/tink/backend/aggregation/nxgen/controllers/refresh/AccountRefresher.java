package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.libraries.metrics.MetricId;

public interface AccountRefresher extends Refresher {
    MetricId METRIC_ID = REFRESHER_METRIC_ID.label(METRIC_ITEM_TYPE, "accounts");
    String METRIC_ACCOUNT_TYPE = "account_type";
    List<Integer> METRIC_COUNTER_BUCKETS = ImmutableList.<Integer>builder()
            .add(0)
            .add(1)
            .add(2)
            .add(3)
            .add(5)
            .add(8)
            .add(13)
            .add(21)
            .build();
    void refreshAccounts();
}
