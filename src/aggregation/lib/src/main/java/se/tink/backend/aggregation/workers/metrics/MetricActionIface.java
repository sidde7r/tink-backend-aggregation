package se.tink.backend.aggregation.workers.metrics;

import java.util.List;

public interface MetricActionIface {

    void start();

    void start(List<? extends Number> metricBuckets);

    void stop();

    void completed();

    void failed();

    void cancelled();

    void unavailable();

    enum Outcome {
        COMPLETED,
        FAILED,
        CANCELLED,
        UNAVAILABLE;

        protected String getMetricName() {
            return name().toLowerCase();
        }
    }
}
