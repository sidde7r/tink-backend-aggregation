package se.tink.backend.aggregation.cluster.provider;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.name.Named;
import com.sun.jersey.api.core.HttpRequestContext;
import se.tink.backend.aggregation.cluster.exception.ClusterNotValid;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.core.ClusterHostConfiguration;

import javax.inject.Inject;
import java.util.Map;

public class ClusterIdProvider {

    private static Map<String, ClusterHostConfiguration> clusterHostConfigurations;
    private boolean isAggregationCluster;

    @Inject
    public ClusterIdProvider(
            @Named("clusterHostConfigurations") Map<String, ClusterHostConfiguration> clusterHostConfigurations,
            @Named("isAggregationCluster") boolean isAggregationCluster) {
        this.clusterHostConfigurations = clusterHostConfigurations;
        this.isAggregationCluster = isAggregationCluster;
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

    private Aggregator createAggregator(String aggregationName, ClusterHostConfiguration configuration) {
        if (!Strings.isNullOrEmpty(aggregationName)) {
            return Aggregator.of(aggregationName);
        }

        if (!Strings.isNullOrEmpty(configuration.getAggregatorIdentifier())) {
            return Aggregator.of(configuration.getAggregatorIdentifier());
        }


        return Aggregator.of(Aggregator.DEFAULT);
    }

    public ClusterInfo getClusterInfo(String clusterName, String clusterEnvironment, String aggregatorName) throws ClusterNotValid {
        ClusterId clusterId;
        if (!isAggregationCluster) {
            clusterId = ClusterId.createEmpty();
            return ClusterInfo.createForLegacyAggregation(clusterId);
        }

        ClusterHostConfiguration configuration = getValidClusterHost(clusterName, clusterEnvironment);
        Aggregator aggregator = createAggregator(aggregatorName, configuration);

        clusterId = ClusterId.create(clusterName,
                clusterEnvironment,
                aggregator);

        return  ClusterInfo.createForAggregationCluster(clusterId,
                configuration.getHost(),
                configuration.getApiToken(),
                configuration.getClientCertificate(),
                configuration.isDisableRequestCompression());
    }

}