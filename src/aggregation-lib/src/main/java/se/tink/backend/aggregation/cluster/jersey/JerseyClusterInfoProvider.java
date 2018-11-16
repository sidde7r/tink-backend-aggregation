package se.tink.backend.aggregation.cluster.jersey;

import com.google.inject.Inject;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import se.tink.backend.aggregation.cluster.annotations.ClusterContext;
import se.tink.backend.aggregation.cluster.exceptions.ClusterNotValid;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.storage.database.providers.ClusterInfoProvider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;

public class JerseyClusterInfoProvider extends AbstractHttpContextInjectable<ClusterInfo>
        implements InjectableProvider<ClusterContext, Type> {

    protected static final String CLUSTER_NAME_HEADER = ClusterId.CLUSTER_NAME_HEADER;
    protected static final String CLUSTER_ENVIRONMENT_HEADER = ClusterId.CLUSTER_ENVIRONMENT_HEADER;
    private ClusterInfoProvider clusterInfoProvider;

    @Inject
    public JerseyClusterInfoProvider(ClusterInfoProvider clusterInfoProvider) {
        this.clusterInfoProvider = clusterInfoProvider;
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
        String name = request.getHeaderValue(CLUSTER_NAME_HEADER);
        String environment = request.getHeaderValue(CLUSTER_ENVIRONMENT_HEADER);

        try {
            return clusterInfoProvider.getClusterInfo(name, environment);
        } catch (ClusterNotValid clusterNotValid) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
