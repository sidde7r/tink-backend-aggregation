package se.tink.backend.aggregation.configurations.providers;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.cluster.exceptions.ClusterNotValid;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.core.ClusterHostConfiguration;

import javax.inject.Inject;
import java.util.Map;

public class ClusterInfoProvider {

    private static Map<String, ClusterHostConfiguration> clusterHostConfigurations;

    @Inject
    public ClusterInfoProvider(
            @Named("clusterHostConfigurations") Map<String, ClusterHostConfiguration> clusterHostConfigurations) {
        this.clusterHostConfigurations = clusterHostConfigurations;
    }

    private void validateClusterHostConfiguration(ClusterHostConfiguration configuration) {
        Preconditions.checkNotNull(configuration);
        Preconditions.checkNotNull(configuration.getHost());
        Preconditions.checkNotNull(configuration.getApiToken());
        Preconditions.checkNotNull(configuration.getClientCertificate());
        Preconditions.checkNotNull(configuration.getAggregatorIdentifier());
    }

    private ClusterHostConfiguration getValidClusterHost(String clusterName, String clusterEnvironment) throws ClusterNotValid {
        if (Strings.isNullOrEmpty(clusterName) || Strings.isNullOrEmpty(clusterEnvironment)) {
            throw new ClusterNotValid();
        }

        ClusterHostConfiguration configuration = clusterHostConfigurations
                .get(String.format("%s-%s", clusterName, clusterEnvironment));

        validateClusterHostConfiguration(configuration);

        return configuration;
    }

    private Aggregator createAggregator(ClusterHostConfiguration configuration) {
        if (!Strings.isNullOrEmpty(configuration.getAggregatorIdentifier())) {
            return Aggregator.of(configuration.getAggregatorIdentifier());
        }

        return Aggregator.of(Aggregator.DEFAULT);
    }

    public ClusterInfo getClusterInfo(String clusterName, String clusterEnvironment) throws ClusterNotValid {
        ClusterHostConfiguration configuration = getValidClusterHost(clusterName, clusterEnvironment);
        Aggregator aggregator = createAggregator(configuration);

        ClusterId clusterId = ClusterId.of(clusterName,
                clusterEnvironment);

        return  ClusterInfo.createForAggregationCluster(clusterId,
                configuration.getHost(),
                configuration.getApiToken(),
                configuration.getClientCertificate(),
                configuration.isDisableRequestCompression(),
                aggregator);
    }
}
