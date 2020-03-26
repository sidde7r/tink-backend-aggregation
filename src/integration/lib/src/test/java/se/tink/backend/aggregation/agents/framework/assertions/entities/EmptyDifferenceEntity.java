package se.tink.backend.aggregation.agents.framework.assertions.entities;

public class EmptyDifferenceEntity implements DifferenceEntity {

    private final Object expected;
    private final Object given;

    public EmptyDifferenceEntity(Object expected, Object given) {
        this.expected = expected;
        this.given = given;
    }

    public Object getExpected() {
        return expected;
    }

    public Object getGiven() {
        return given;
    }
}
