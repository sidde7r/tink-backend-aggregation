package se.tink.backend.aggregation.storage.file.models;

public class ClientConfig {
    private String aggregatorIdentifier;

    public String getAggregatorIdentifier() {
        return aggregatorIdentifier;
    }

    public void setAggregatorIdentifier(String aggregatorIdentifier) {
        this.aggregatorIdentifier = aggregatorIdentifier;
    }
}
