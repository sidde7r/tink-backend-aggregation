package se.tink.backend.aggregation.workers.metrics;

import java.util.List;

public interface MetricActionIface {

    void start();

    void start(List<? extends Number> metricBuckets);

    void stop();

    void completed();

    void failed();

    void cancelled();

    void cancelledDueToThirdPartyAppTimeout();

    void unavailable();

    void partiallyCompleted();
}
