package se.tink.backend.insights.core.valueobjects;

public enum TinkInsightScore {
    URGENT(100),
    HIGH(10),
    NORMAL(5),
    LOW(1);

    private final int value;

    TinkInsightScore(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
