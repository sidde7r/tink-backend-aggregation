package se.tink.backend.aggregation.api;

public class AggregatorInfo {
    private String aggregatorIdentifier;

    public String getAggregatorIdentifier() {
        return aggregatorIdentifier;
    }

    public void setAggregatorIdentifier(String aggregatorIdentifier) {
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    public static AggregatorInfo getAggregatorForTesting() {
        AggregatorInfo aggregatorInfo = new AggregatorInfo();

        aggregatorInfo.setAggregatorIdentifier("Tink Testing");

        return aggregatorInfo;
    }
}
