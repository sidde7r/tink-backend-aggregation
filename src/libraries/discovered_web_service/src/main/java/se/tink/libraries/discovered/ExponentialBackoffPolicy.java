package se.tink.libraries.discovered;

public class ExponentialBackoffPolicy {
    private long nextSleepDuration;
    private long initialSleep;
    private boolean firstRetry;

    public ExponentialBackoffPolicy(long initialSleep) {
        this.firstRetry = true;
        this.nextSleepDuration = 0;
        this.initialSleep = initialSleep;
    }

    public long getSleepDuration() {
        if (firstRetry) {
            // First retry is for free. No sleeping.
            firstRetry = false;
            nextSleepDuration = initialSleep;
            return 0;
        } else {
            long result = nextSleepDuration;
            nextSleepDuration *= 2;
            return result;
        }
    }
}
