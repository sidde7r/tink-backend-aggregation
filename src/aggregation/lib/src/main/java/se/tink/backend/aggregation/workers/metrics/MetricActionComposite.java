package se.tink.backend.aggregation.workers.metrics;

import java.util.Arrays;
import java.util.List;

public class MetricActionComposite implements MetricActionIface {

    private final List<MetricActionIface> metricActions;

    public MetricActionComposite(MetricActionIface... metricActions) {
        this.metricActions = Arrays.asList(metricActions);
    }

    @Override
    public void start() {
        metricActions.forEach(MetricActionIface::start);
    }

    @Override
    public void start(List<? extends Number> metricBuckets) {
        metricActions.forEach(ma -> ma.start(metricBuckets));
    }

    @Override
    public void stop() {
        metricActions.forEach(MetricActionIface::stop);
    }

    @Override
    public void completed() {
        metricActions.forEach(MetricActionIface::completed);
    }

    @Override
    public void failed() {
        metricActions.forEach(MetricActionIface::failed);
    }

    @Override
    public void cancelled() {
        metricActions.forEach(MetricActionIface::cancelled);
    }

    @Override
    public void cancelledDueToThirdPartyAppTimeout() {
        metricActions.forEach(MetricActionIface::cancelledDueToThirdPartyAppTimeout);
    }

    @Override
    public void unavailable() {
        metricActions.forEach(MetricActionIface::unavailable);
    }

    @Override
    public void partiallyCompleted() {
        metricActions.forEach(MetricActionIface::partiallyCompleted);
    }
}
