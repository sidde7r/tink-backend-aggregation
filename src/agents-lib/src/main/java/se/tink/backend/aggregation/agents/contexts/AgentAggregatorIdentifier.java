package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.api.AggregatorInfo;

public interface AgentAggregatorIdentifier {
    AggregatorInfo getAggregatorInfo();

    void setAggregatorInfo(AggregatorInfo aggregatorInfo);
}
