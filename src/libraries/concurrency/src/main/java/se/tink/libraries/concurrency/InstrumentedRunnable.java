package se.tink.libraries.concurrency;

import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class InstrumentedRunnable implements Runnable {
    private final MetricId METRIC_ID_BASE = MetricId.newId("instrumented_runnable");

    private final Counter started;
    private final Counter queued;
    private final Counter finished;

    private final Runnable delegate;

    public InstrumentedRunnable(
            MetricRegistry registry,
            String name,
            MetricId.MetricLabels metricLabels,
            Runnable delegate) {

        MetricId serviceBaseName = METRIC_ID_BASE.label("name", name).label(metricLabels);

        this.started = registry.meter(serviceBaseName.label("event", "started"));
        this.queued = registry.meter(serviceBaseName.label("event", "queued"));
        this.finished = registry.meter(serviceBaseName.label("event", "finished"));
        this.delegate = RunnableMdcWrapper.wrap(delegate);
    }

    public void submitted() {
        queued.inc();
    }

    @Override
    public void run() {
        started.inc();
        try {
            delegate.run();
        } finally {
            finished.inc();
        }
    }
}
