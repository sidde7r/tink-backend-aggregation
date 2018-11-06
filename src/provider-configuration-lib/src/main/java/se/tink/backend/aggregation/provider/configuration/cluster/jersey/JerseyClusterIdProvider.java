package se.tink.backend.aggregation.provider.configuration.cluster.jersey;

import com.google.inject.Inject;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.provider.configuration.cluster.annotations.ClusterContext;
import se.tink.backend.aggregation.provider.configuration.cluster.exceptions.ClusterNotValid;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterId;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterInfo;
import se.tink.backend.aggregation.provider.configuration.cluster.providers.ClusterIdProvider;

public class JerseyClusterIdProvider extends AbstractHttpContextInjectable<ClusterInfo>
        implements InjectableProvider<ClusterContext, Type> {

    private static final String CLUSTER_NAME_HEADER = ClusterId.CLUSTER_NAME_HEADER;
    private static final String CLUSTER_ENVIRONMENT_HEADER = ClusterId.CLUSTER_ENVIRONMENT_HEADER;
    private ClusterIdProvider clusterIdProvider;

    @Inject
    public JerseyClusterIdProvider(ClusterIdProvider clusterIdProvider) {
        this.clusterIdProvider = clusterIdProvider;
    }

    @Override
    public Injectable<ClusterInfo> getInjectable(ComponentContext ic, ClusterContext a, Type c) {
        return c.equals(ClusterInfo.class) ? this : null;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public ClusterInfo getValue(HttpContext c) {
        HttpRequestContext request = c.getRequest();
        String name = request.getHeaderValue(CLUSTER_NAME_HEADER);
        String environment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);

        try {
            return clusterIdProvider.getClusterInfo(name, environment);
        } catch (ClusterNotValid clusterNotValid) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
