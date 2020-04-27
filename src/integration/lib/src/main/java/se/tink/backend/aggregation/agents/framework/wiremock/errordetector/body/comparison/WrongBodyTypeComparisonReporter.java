package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison;

public class WrongBodyTypeComparisonReporter implements ComparisonReporter {

    @Override
    public String report() {
        return "Body types are not matching!";
    }

    @Override
    public boolean isThereDifference() {
        return true;
    }
}
