package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison;

import se.tink.backend.aggregation.comparor.DifferenceEntity;
import se.tink.backend.aggregation.comparor.MapDifferenceEntity;

public class ListComparisonReporter implements ComparisonReporter {

    private final Integer expectedListSize;
    private final Integer givenListSize;
    private final ComparisonReporter comparisonReporter;

    public ListComparisonReporter(
            int givenListSize, int expectedListSize, DifferenceEntity differenceEntity) {
        this.expectedListSize = expectedListSize;
        this.givenListSize = givenListSize;
        if (differenceEntity instanceof MapDifferenceEntity) {
            this.comparisonReporter = new MapComparisonReporter(differenceEntity);
        } else {
            this.comparisonReporter = new NoDifferenceComparisonReporter();
        }
    }

    @Override
    public String report() {

        StringBuilder errorMessage = new StringBuilder();
        if (!expectedListSize.equals(givenListSize)) {
            errorMessage.append(
                    "List size are different (expected "
                            + expectedListSize
                            + " , given "
                            + givenListSize
                            + ")");
        } else {
            errorMessage.append(comparisonReporter.report());
        }
        return errorMessage.toString();
    }

    @Override
    public boolean isThereDifference() {
        return (!expectedListSize.equals(givenListSize)) || comparisonReporter.isThereDifference();
    }

    public Integer getExpectedListSize() {
        return expectedListSize;
    }

    public Integer getGivenListSize() {
        return givenListSize;
    }
}
