package se.tink.backend.aggregation.cluster.identification;

public class Aggregator {

    private String aggregatorIdentifier;
    public static final String DEFAULT = "Tink";

    private Aggregator(String aggregatorIdentifier) {
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    public static Aggregator getDefault() {
        return new Aggregator(DEFAULT);
    }

    public String getAggregatorIdentifier() {
        return this.aggregatorIdentifier;
    }

    public void setAggregatorIdentifier(String aggregatorIdentifier) {
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    public static Aggregator of(String aggregatorIdentifier) {
        return new Aggregator(aggregatorIdentifier);
    }
}
