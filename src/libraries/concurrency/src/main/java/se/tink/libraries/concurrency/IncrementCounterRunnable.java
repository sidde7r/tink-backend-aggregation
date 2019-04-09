package se.tink.libraries.concurrency;

import se.tink.libraries.metrics.Counter;

/** Thread-safe. */
public class IncrementCounterRunnable implements Runnable {
    private final Counter counter;

    public IncrementCounterRunnable(Counter counter) {
        this.counter = counter;
    }

    @Override
    public void run() {
        counter.inc();
    }
}
