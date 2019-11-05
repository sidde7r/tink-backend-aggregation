package se.tink.backend.aggregation.workers.metrics;

import java.util.Arrays;
import java.util.List;

public class MetricActionComposite implements MetricActionIface {

    private List<MetricActionIface> metricActions;

    public MetricActionComposite(MetricActionIface... metricActions) {
        this.metricActions = Arrays.asList(metricActions);
    }

    @Override
    public void start() {
        metricActions.forEach(ma -> ma.start());
    }

    @Override
    public void start(List<? extends Number> metricBuckets) {
        metricActions.forEach(ma -> ma.start(metricBuckets));
    }

    @Override
    public void stop() {
        metricActions.forEach(ma -> ma.stop());
    }

    @Override
    public void completed() {
        metricActions.forEach(ma -> ma.completed());
    }

    @Override
    public void failed() {
        metricActions.forEach(ma -> ma.failed());
    }

    @Override
    public void cancelled() {
        metricActions.forEach(ma -> ma.cancelled());
    }

    @Override
    public void unavailable() {
        metricActions.forEach(ma -> ma.unavailable());
    }
}
