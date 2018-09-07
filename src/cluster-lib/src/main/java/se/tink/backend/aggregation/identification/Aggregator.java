package se.tink.backend.aggregation.cluster.identification;

import com.google.common.base.Strings;

public class Aggregator {

    private String aggregatorIdentifier;
    public final static String DEFAULT = "Tink";

    public Aggregator() {
    }

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
