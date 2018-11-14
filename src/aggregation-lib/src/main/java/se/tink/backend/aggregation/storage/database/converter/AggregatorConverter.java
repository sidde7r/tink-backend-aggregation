package se.tink.backend.aggregation.storage.database.converter;

import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;

public class AggregatorConverter {
    public static AggregatorInfo convert(Aggregator aggregator) {
        AggregatorInfo aggregatorInfo = new AggregatorInfo();

        aggregatorInfo.setAggregatorIdentifier(aggregator.getAggregatorIdentifier());

        return aggregatorInfo;
    }

    public static AggregatorInfo convert(AggregatorConfiguration aggregatorConfiguration) {
        AggregatorInfo aggregatorInfo = new AggregatorInfo();

        aggregatorInfo.setAggregatorIdentifier(aggregatorConfiguration.getAggregatorInfo());

        return aggregatorInfo;
    }
}
