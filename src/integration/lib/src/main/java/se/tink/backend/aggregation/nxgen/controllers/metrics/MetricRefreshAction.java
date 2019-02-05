package se.tink.backend.aggregation.nxgen.controllers.metrics;

import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class MetricRefreshAction extends MetricAction {
    private final List<? extends Number> counterBuckets;

    MetricRefreshAction(Credentials credentials, MetricRegistry registry, MetricId metricId,
            List<? extends Number> counterBuckets) {
        super(credentials, registry, metricId);
        this.counterBuckets = counterBuckets;
    }

    public void count(double value) {
        registry.histogram(metricId.suffix("history"), counterBuckets).update(value);
    }
}
