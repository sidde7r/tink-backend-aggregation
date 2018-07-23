package se.tink.backend.aggregation.injectableproviders;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Type;
import se.tink.backend.aggregation.cluster.identification.Aggregator;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.common.repository.mysql.aggregation.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;

@Provider
public class ClusterIdProvider extends AbstractHttpContextInjectable<ClusterId>
        implements InjectableProvider<ClusterContext, Type> {

    private static final String CLUSTER_NAME_HEADER = "x-tink-cluster-name";
    private static final String CLUSTER_ENVIRONMENT_HEADER = "x-tink-cluster-environment";
    private static final String AGGREGATOR_NAME_HEADER = "x-tink-aggregator-header";
    private ClusterHostConfigurationRepository clusterHostConfigurationRepository;
    private HttpServletRequest httpRequest;
    private boolean isAggregationCluster;

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

        ClusterHostConfiguration configuration = clusterHostConfigurationRepository
                .findOne(String.format("%s-%s", name, environment));

        validateClusterHostConfiguration(configuration);

        return configuration;
    }

    private Aggregator createAggregator(HttpRequestContext request, ClusterHostConfiguration configuration) {
        String customAggregator = request.getHeaderValue(AGGREGATOR_NAME_HEADER);
        if (!(Objects.isNull(customAggregator) || customAggregator.equals(""))) {
            return Aggregator.of(customAggregator);
        }

        if (!(Objects.isNull(configuration.getAggregatorIdentifier()) ||
                configuration.getAggregatorIdentifier().equals(""))) {
            return Aggregator.of(configuration.getAggregatorIdentifier());
        }

        return Aggregator.of(Aggregator.DEFAULT);
    }


    private ClusterId getClusterId(HttpRequestContext request) {
        if (!isAggregationCluster) {
            return ClusterId.createEmpty();
        }

        ClusterHostConfiguration configuration = getValidClusterHost(request);
        Aggregator aggregator = createAggregator(request, configuration);

        return ClusterId.create(request.getHeaderValue(CLUSTER_NAME_HEADER),
                request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER),
                aggregator);

    }

    public ClusterIdProvider(ClusterHostConfigurationRepository clusterHostConfigurationRepository, boolean isAggregationCluster) {
        this.clusterHostConfigurationRepository = clusterHostConfigurationRepository;
        this.isAggregationCluster = isAggregationCluster;
    }

    /**
     * From interface InjectableProvider
     */
    @Override
    public Injectable<ClusterId> getInjectable(ComponentContext ic, ClusterContext a, Type c) {
        if (c.equals(ClusterId.class)) {
            return this;
        }
        return null;
    }

    /**
     * From interface InjectableProvider
     * A new Injectable is instanciated per request
     */
    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    /**
     * From interface Injectable
     * Get the logged User associated with the request
     * Or throw an Unauthorized HTTP status code
     */
    @Override
    public ClusterId getValue(HttpContext c) {
        HttpRequestContext request = c.getRequest();
        return getClusterId(request);
    }
}