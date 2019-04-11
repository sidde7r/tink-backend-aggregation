package se.tink.backend.integration.fetchservice.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class AggregatorInfo {
    private final String clientId;
    private final String aggregatorIdentifier;

    private AggregatorInfo(String clientId, String aggregatorIdentifier) {
        this.clientId = clientId;
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    public static AggregatorInfo of(String clientId, String aggregatorIdentifier) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clientId));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aggregatorIdentifier));
        return new AggregatorInfo(clientId, aggregatorIdentifier);
    }

    public String getClientId() {
        return clientId;
    }

    public String getAggregatorIdentifier() {
        return aggregatorIdentifier;
    }
}
