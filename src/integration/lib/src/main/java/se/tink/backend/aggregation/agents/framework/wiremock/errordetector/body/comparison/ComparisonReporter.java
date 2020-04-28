package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison;

public interface ComparisonReporter {
    String report();

    boolean isThereDifference();
}
