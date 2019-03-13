package se.tink.backend.integration.fetchservice.fetchcontroller;

class AggregatorInfo {
    private final String clientId;
    private final String aggregatorIdentifier;

    private AggregatorInfo(String clientId, String aggregatorIdentifier) {
        this.clientId = clientId;
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    static AggregatorInfo of(se.tink.backend.integration.api.models.AggregatorInfo aggregatorInfo) {
        return new AggregatorInfo(aggregatorInfo.getClientId(), aggregatorInfo.getAggregatorIdentifier());
    }

    String getClientId() {
        return clientId;
    }

    String getAggregatorIdentifier() {
        return aggregatorIdentifier;
    }
}
