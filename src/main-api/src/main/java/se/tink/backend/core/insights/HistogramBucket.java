package se.tink.backend.core.insights;

import io.protostuff.Tag;
import java.math.BigDecimal;

public class HistogramBucket {
    @Tag(1)
    private final BigDecimal lowerEndpoint; // Inclusive
    @Tag(2)
    private final BigDecimal upperEndpoint; // Exclusive
    @Tag(3)
    private final BigDecimal frequency;

    public HistogramBucket(BigDecimal lowerEndpoint, BigDecimal upperEndpoint, BigDecimal frequency) {
        this.lowerEndpoint = lowerEndpoint;
        this.upperEndpoint = upperEndpoint;
        this.frequency = frequency;
    }

    public BigDecimal getLowerEndpoint() {
        return lowerEndpoint;
    }

    public BigDecimal getUpperEndpoint() {
        return upperEndpoint;
    }

    public BigDecimal getFrequency() {
        return frequency;
    }
}
