package se.tink.backend.aggregation.storage.database.daos;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.storage.database.converter.HostConfigurationConverter;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;

import java.util.Map;

public class ClusterConfigurationDao {

    private final Map<String, ClusterConfiguration> clusterConfigurations;
    private final boolean isMultiClientDevelopment;

    /*
        we want to use providers to ensure we only load it once when we start the service
        this configuration should not change during the service lifecycle
     */
    @Inject
    public ClusterConfigurationDao(
            @Named("clusterConfigurations") Map<String, ClusterConfiguration> clusterConfigurations,
            @Named("isMultiClientDevelopment") boolean isMultiClientDevelopment) {
        this.clusterConfigurations = clusterConfigurations;
        this.isMultiClientDevelopment = isMultiClientDevelopment;
    }

    public ControllerWrapper createControllerWrapper(
            ClusterInfo clusterInfo,
            AggregationControllerAggregationClient aggregationControllerAggregationClient){

        if(isMultiClientDevelopment) {
            String clusterId = clusterInfo.getClusterId().getId();
            return ControllerWrapper.of(aggregationControllerAggregationClient,
                    HostConfigurationConverter.convert(clusterConfigurations.get(clusterId)));
        }

        return ControllerWrapper.of(aggregationControllerAggregationClient, HostConfigurationConverter.convert(clusterInfo));
    }
}
