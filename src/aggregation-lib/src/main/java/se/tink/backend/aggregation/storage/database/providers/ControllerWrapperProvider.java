package se.tink.backend.aggregation.storage.database.providers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.storage.database.converter.HostConfigurationConverter;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;

import java.util.Map;

public class ControllerWrapperProvider {

    private final Map<String, ClusterConfiguration> clusterConfigurations;
    private final AggregationControllerAggregationClient aggregationControllerAggregationClient;

    /*
        we want to use providers to ensure we only load it once when we start the service
        this configuration should not change during the service lifecycle
     */
    @Inject
    private ControllerWrapperProvider(
            @Named("clusterConfigurations") Map<String, ClusterConfiguration> clusterConfigurations,
            AggregationControllerAggregationClient aggregationControllerAggregationClient) {
        this.clusterConfigurations = clusterConfigurations;
        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;
    }

    public ControllerWrapper createControllerWrapper(String clusterId) {
        return ControllerWrapper.of(aggregationControllerAggregationClient,
                HostConfigurationConverter.convert(clusterConfigurations.get(clusterId)));
    }
}
