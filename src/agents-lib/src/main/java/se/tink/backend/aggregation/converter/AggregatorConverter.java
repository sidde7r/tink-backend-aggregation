package se.tink.backend.aggregation.converter;

import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.cluster.identification.Aggregator;

public class AggregatorConverter {
    public static AggregatorInfo convert(Aggregator aggregator) {
        AggregatorInfo aggregatorInfo = new AggregatorInfo();

        aggregatorInfo.setAggregatorIdentifier(aggregator.getAggregatorIdentifier());

        return aggregatorInfo;
    }
}
