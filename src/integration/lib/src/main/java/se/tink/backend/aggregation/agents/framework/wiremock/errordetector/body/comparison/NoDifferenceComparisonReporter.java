package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison;

public class NoDifferenceComparisonReporter implements ComparisonReporter {

    @Override
    public String report() {
        return "";
    }

    @Override
    public boolean isThereDifference() {
        return false;
    }
}
