package se.tink.backend.aggregation.nxgen.controllers.refresh;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.metrics.core.MetricId;

public interface TransactionRefresher extends Refresher {
    MetricId METRIC_ID = REFRESHER_METRIC_ID;
    List<Integer> METRIC_COUNTER_BUCKETS =
            ImmutableList.<Integer>builder()
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

    @Deprecated
    Map<Account, List<Transaction>> fetchTransactions();
}
