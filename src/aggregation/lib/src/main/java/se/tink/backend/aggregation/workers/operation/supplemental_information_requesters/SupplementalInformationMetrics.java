package se.tink.backend.aggregation.workers.operation.supplemental_information_requesters;

import java.util.Arrays;
import java.util.List;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

class SupplementalInformationMetrics {
    private static final String CLUSTER_LABEL = "client_cluster";
    private static final String INITIATOR = "initiator";
    private static final String WAITER_CLASS = "waiter_class";
    private static final String FINAL_STATUS = "final_status";
    private static final String MARKET = "market";

    private static final MetricId DURATION_METRIC =
            MetricId.newId("aggregation_supplemental_information_seconds");
    private static final MetricId DURATION_PER_MARKET_METRIC =
            MetricId.newId("aggregation_supplemental_information_per_market_seconds");
    private static final MetricId OVERHEAD_DURATION_METRICS =
            MetricId.newId("aggregation_supplemental_information_looping_overhead_duration");
    public static final MetricId attempts =
            MetricId.newId("aggregation_supplemental_information_requests_started");
    public static final MetricId finished =
            MetricId.newId("aggregation_supplemental_information_requests_finished");
    public static final MetricId finished_with_empty =
            MetricId.newId("aggregation_supplemental_information_requests_finished_empty");
    public static final MetricId cancelled =
            MetricId.newId("aggregation_supplemental_information_requests_cancelled");
    public static final MetricId timedOut =
            MetricId.newId("aggregation_supplemental_information_requests_timed_out");
    public static final MetricId error =
            MetricId.newId("aggregation_supplemental_information_requests_error");
    private static final List<Integer> buckets =
            Arrays.asList(
                    0, 10, 20, 30, 40, 50, 60, 80, 100, 120, 240, 270, 300, 360, 420, 480, 600);

    public static void inc(
            MetricRegistry registry,
            MetricId metricId,
            String clusterId,
            String initiator,
            String waiterClass) {
        MetricId metricIdWithLabel =
                metricId.label(CLUSTER_LABEL, clusterId)
                        .label(INITIATOR, initiator)
                        .label(WAITER_CLASS, waiterClass);
        registry.meter(metricIdWithLabel).inc();
    }

    public static void observeTotalTime(
            MetricRegistry metricRegistry,
            long duration,
            String initiator,
            String waiterClass,
            String market,
            SupplementalInformationWaiterFinalStatus finalStatus) {
        metricRegistry
                .histogram(
                        DURATION_METRIC
                                .label(INITIATOR, initiator)
                                .label(WAITER_CLASS, waiterClass)
                                .label(FINAL_STATUS, finalStatus.toString()),
                        buckets)
                .update(duration);

        metricRegistry
                .histogram(
                        DURATION_PER_MARKET_METRIC
                                .label(MARKET, market)
                                .label(FINAL_STATUS, finalStatus.toString()),
                        buckets)
                .update(duration);
    }

    public static void observeOverheadTime(
            MetricRegistry metricRegistry,
            long duration,
            String initiator,
            String waiterClass,
            SupplementalInformationWaiterFinalStatus finalStatus) {
        metricRegistry
                .histogram(
                        OVERHEAD_DURATION_METRICS
                                .label(INITIATOR, initiator)
                                .label(WAITER_CLASS, waiterClass)
                                .label(FINAL_STATUS, finalStatus.toString()),
                        buckets)
                .update(duration);
    }
}
