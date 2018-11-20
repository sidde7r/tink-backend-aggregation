package se.tink.backend.aggregation.storage.database.providers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.cluster.exceptions.ClientNotValid;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.storage.database.converter.AggregatorConverter;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;

import java.util.Map;

public class AggregatorInfoProvider {

    private final Map<String, AggregatorConfiguration> aggregatorConfigurations;

    @Inject
    AggregatorInfoProvider(
            @Named("aggregatorConfiguration") Map<String, AggregatorConfiguration> aggregatorConfigurations) {
        this.aggregatorConfigurations = aggregatorConfigurations;
    }

    // using aggregatorId (UUID) to get aggregator in multi client environment
    public AggregatorInfo createAggregatorInfoFor(String aggregatorId) {
        return AggregatorConverter.convert(aggregatorConfigurations.get(aggregatorId));
    }
}
