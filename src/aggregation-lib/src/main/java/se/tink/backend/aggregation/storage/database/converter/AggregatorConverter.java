package se.tink.backend.aggregation.storage.database.converter;

import com.google.common.base.Preconditions;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;

public class AggregatorConverter {

    public static AggregatorInfo convert(AggregatorConfiguration aggregatorConfiguration) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aggregatorConfiguration.getAggregatorInfo()),
                "Could not find aggregator identifier value for aggregator id "+aggregatorConfiguration.getAggregatorId()+".");
        AggregatorInfo aggregatorInfo = new AggregatorInfo();

        aggregatorInfo.setAggregatorIdentifier(aggregatorConfiguration.getAggregatorInfo());

        return aggregatorInfo;
    }
}
