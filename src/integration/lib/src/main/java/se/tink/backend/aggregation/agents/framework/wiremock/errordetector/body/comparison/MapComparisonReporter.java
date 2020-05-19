package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison;

import com.google.common.collect.ImmutableSet;
import se.tink.backend.aggregation.comparor.DifferenceEntity;
import se.tink.backend.aggregation.comparor.EmptyDifferenceEntity;
import se.tink.backend.aggregation.comparor.MapDifferenceEntity;

public class MapComparisonReporter implements ComparisonReporter {

    private final DifferenceEntity differenceEntity;

    private final ImmutableSet<String> missingBodyKeysInGivenRequest;
    private final ImmutableSet<String> bodyKeysWithDifferentValue;

    public MapComparisonReporter(DifferenceEntity differenceEntity) {
        this.differenceEntity = differenceEntity;

        if (differenceEntity instanceof MapDifferenceEntity) {
            missingBodyKeysInGivenRequest =
                    ImmutableSet.copyOf(
                            ((MapDifferenceEntity) differenceEntity)
                                    .getEntriesOnlyOnExpected()
                                    .keySet());
            bodyKeysWithDifferentValue =
                    ImmutableSet.copyOf(
                            ((MapDifferenceEntity) differenceEntity)
                                    .getDifferenceInCommonKeys()
                                    .keySet());
        } else if (differenceEntity instanceof EmptyDifferenceEntity) {
            missingBodyKeysInGivenRequest = ImmutableSet.of();
            bodyKeysWithDifferentValue = ImmutableSet.of();
        } else {
            throw new IllegalStateException("Should not reach here!");
        }
    }

    public DifferenceEntity getDifferenceEntity() {
        return differenceEntity;
    }

    @Override
    public String report() {

        StringBuilder errorMessage = new StringBuilder();

        if (!missingBodyKeysInGivenRequest.isEmpty() || !bodyKeysWithDifferentValue.isEmpty()) {
            errorMessage.append("There is a mismatch between request bodies\n");
            errorMessage.append("The differences are the following:\n");
            if (!missingBodyKeysInGivenRequest.isEmpty()) {
                errorMessage.append("The following keys only appear in expected object\n");
                missingBodyKeysInGivenRequest.forEach(key -> errorMessage.append(key).append("\n"));
            }
            if (!bodyKeysWithDifferentValue.isEmpty()) {
                errorMessage.append(
                        "For the following keys the expected and given objects have different values\n");
                bodyKeysWithDifferentValue.forEach(key -> errorMessage.append(key).append("\n"));
            }
        }
        return errorMessage.toString();
    }

    @Override
    public boolean isThereDifference() {
        return !(differenceEntity instanceof EmptyDifferenceEntity);
    }

    public ImmutableSet<String> getMissingBodyKeysInGivenRequest() {
        return missingBodyKeysInGivenRequest;
    }

    public ImmutableSet<String> getBodyKeysWithDifferentValue() {
        return bodyKeysWithDifferentValue;
    }
}
