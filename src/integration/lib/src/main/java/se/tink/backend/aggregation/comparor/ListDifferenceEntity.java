package se.tink.backend.aggregation.comparor;

public class ListDifferenceEntity implements DifferenceEntity {

    private final int expectedListSize;
    private final int givenListSize;

    public ListDifferenceEntity(int expectedListSize, int givenListSize) {
        this.expectedListSize = expectedListSize;
        this.givenListSize = givenListSize;
    }

    public int getExpectedListSize() {
        return expectedListSize;
    }

    public int getGivenListSize() {
        return givenListSize;
    }
}
