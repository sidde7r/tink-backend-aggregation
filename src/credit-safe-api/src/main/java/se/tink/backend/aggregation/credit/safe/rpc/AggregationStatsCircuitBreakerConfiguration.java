package se.tink.backend.aggregation.credit.safe.rpc;

import java.util.concurrent.TimeUnit;

public class AggregationStatsCircuitBreakerConfiguration {

    private double failRatioThreshold;
    private int minimumTrials;
    private String mode;
    private int timeWindow;
    private TimeUnit timeWindowUnit;

    public double getFailRatioThreshold() {
        return failRatioThreshold;
    }

    public int getMinimumTrials() {
        return minimumTrials;
    }

    public String getMode() {
        return mode;
    }

    public int getTimeWindow() {
        return timeWindow;
    }

    public TimeUnit getTimeWindowUnit() {
        return timeWindowUnit;
    }

    public void setFailRatioThreshold(double failRatioThreshold) {
        this.failRatioThreshold = failRatioThreshold;
    }

    public void setMinimumTrials(int minimumTrials) {
        this.minimumTrials = minimumTrials;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setTimeWindow(int timeWindow) {
        this.timeWindow = timeWindow;
    }

    public void setTimeWindowUnit(TimeUnit timeWindowUnit) {
        this.timeWindowUnit = timeWindowUnit;
    }

}
