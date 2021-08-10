package se.tink.libraries.concurrency;

import se.tink.libraries.metrics.types.gauges.IncrementDecrementGauge;

public class DecrementGaugeRunnable implements Runnable {
    private final IncrementDecrementGauge gauge;

    public DecrementGaugeRunnable(IncrementDecrementGauge gauge) {
        this.gauge = gauge;
    }

    @Override
    public void run() {
        gauge.decrement();
    }
}
