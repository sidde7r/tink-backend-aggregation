package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison;

public class PlainTextComparisonReporter implements ComparisonReporter {

    private final String expected;
    private final String given;

    public PlainTextComparisonReporter(String expected, String given) {
        this.expected = expected;
        this.given = given;
    }

    @Override
    public String report() {
        return "Data for request bodies are different";
    }

    @Override
    public boolean isThereDifference() {
        return !expected.equals(given);
    }
}
