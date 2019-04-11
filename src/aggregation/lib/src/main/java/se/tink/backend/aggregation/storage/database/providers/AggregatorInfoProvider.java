package se.tink.backend.aggregation.storage.database.providers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Map;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.storage.database.converter.AggregatorConverter;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;

public class AggregatorInfoProvider {

    private final Map<String, AggregatorConfiguration> aggregatorConfigurations;

    @Inject
    AggregatorInfoProvider(
            @Named("aggregatorConfiguration")
                    Map<String, AggregatorConfiguration> aggregatorConfigurations) {
        this.aggregatorConfigurations = aggregatorConfigurations;
    }

    // using aggregatorId (UUID) to get aggregator in multi client environment
    public AggregatorInfo createAggregatorInfoFor(String aggregatorId) {
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(aggregatorId), "Aggregator Id cannot be null or empty.");
        AggregatorConfiguration aggregatorConfiguration =
                aggregatorConfigurations.get(aggregatorId);
        Preconditions.checkNotNull(
                aggregatorConfiguration,
                "Could not find aggregator configuration for aggregatorId %s.",
                aggregatorId);
        return AggregatorConverter.convert(aggregatorConfiguration);
    }
}
