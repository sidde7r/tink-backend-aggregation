package se.tink.backend.aggregation.injectableproviders;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.name.Named;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Type;
import se.tink.backend.aggregation.cluster.annotation.ClusterContext;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.core.ClusterHostConfiguration;

@Provider
public class ClusterIdProvider extends AbstractHttpContextInjectable<ClusterInfo>
        implements InjectableProvider<ClusterContext, Type> {

    private static final String CLUSTER_NAME_HEADER = ClusterId.CLUSTER_NAME_HEADER;
    private static final String CLUSTER_ENVIRONMENT_HEADER = ClusterId.CLUSTER_ENVIRONMENT_HEADER;
    private static final String AGGREGATOR_NAME_HEADER = ClusterId.AGGREGATOR_NAME_HEADER;
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

    private ClusterHostConfiguration getValidClusterHost(HttpRequestContext request) {
        String name = request.getHeaderValue(CLUSTER_NAME_HEADER);
        String environment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);

        if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(environment)) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        ClusterHostConfiguration configuration = clusterHostConfigurations
                .get(String.format("%s-%s", name, environment));

        validateClusterHostConfiguration(configuration);

        return configuration;
    }

    private Aggregator createAggregator(HttpRequestContext request, ClusterHostConfiguration configuration) {
        String customAggregator = request.getHeaderValue(AGGREGATOR_NAME_HEADER);
        if (!Strings.isNullOrEmpty(customAggregator)) {
            return Aggregator.of(customAggregator);
        }

        if (!Strings.isNullOrEmpty(configuration.getAggregatorIdentifier())) {
            return Aggregator.of(configuration.getAggregatorIdentifier());
        }

        return Aggregator.of(Aggregator.DEFAULT);
    }


    private ClusterInfo getClusterId(HttpRequestContext request) {
        ClusterId clusterId;
        if (!isAggregationCluster) {
            clusterId = ClusterId.createEmpty();
            return ClusterInfo.createForLegacyAggregation(clusterId);
        }

        ClusterHostConfiguration configuration = getValidClusterHost(request);
        Aggregator aggregator = createAggregator(request, configuration);

         clusterId = ClusterId.create(request.getHeaderValue(CLUSTER_NAME_HEADER),
                request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER),
                aggregator);

        return  ClusterInfo.createForAggregationCluster(clusterId,
                configuration.getHost(),
                configuration.getApiToken(),
                configuration.getClientCertificate(),
                configuration.isDisableRequestCompression());
    }

    @Override
    public Injectable<ClusterInfo> getInjectable(ComponentContext ic, ClusterContext a, Type c) {
        if (c.equals(ClusterInfo.class)) {
            return this;
        }
        return null;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public ClusterInfo getValue(HttpContext c) {
        HttpRequestContext request = c.getRequest();


        return getClusterId(request);
    }
}