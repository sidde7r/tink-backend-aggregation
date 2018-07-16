package se.tink.backend.aggregation.cluster.identification;

public class Aggregator {

    private final String aggregatorIdentifier;
    public final static String DEFAULT = "Tink (+https://www.tink.se/; noc@tink.se)";

    public Aggregator(String aggregatorIdentifier){
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    public static Aggregator getDefault() {
        return new Aggregator(DEFAULT);
    }

    public String getAggregatorIdentifier(){
        return this.aggregatorIdentifier;
    }

    public static Aggregator of(String aggregatorIdentifier) {
        return new Aggregator(aggregatorIdentifier);
    }

}
