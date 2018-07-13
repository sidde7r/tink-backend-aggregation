package se.tink.backend.aggregation.cluster.identification;

public class Aggregator {

    private final String aggregatorIdentifier;

    public Aggregator(String aggregatorIdentifier){
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    public String getAggregatorIdentifier(){
        return this.aggregatorIdentifier;
    }

}
