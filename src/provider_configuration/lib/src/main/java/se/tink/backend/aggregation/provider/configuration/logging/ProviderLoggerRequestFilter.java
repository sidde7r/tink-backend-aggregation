package se.tink.backend.aggregation.provider.configuration.logging;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.util.Objects;
import org.slf4j.MDC;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterId;

public class ProviderLoggerRequestFilter implements ContainerRequestFilter {
    private static final String CLUSTER_ID_MDC_KEY = "clusterId";

    private void clearMdcKeys() {
        // The values must be removed so that previous thread information is not lingering.
        MDC.remove(CLUSTER_ID_MDC_KEY);
    }

    private void extractClusterId(ContainerRequest request) {
        ClusterId clusterId;

        if (Objects.isNull(request)) {
            clusterId = ClusterId.createEmpty();
        } else {
            String clusterName = request.getHeaderValue(ClusterId.CLUSTER_NAME_HEADER);
            String clusterEnvironment =
                    request.getHeaderValue(ClusterId.CLUSTER_ENVIRONMENT_HEADER);
            clusterId = ClusterId.of(clusterName, clusterEnvironment);
        }

        if (clusterId.isValidId()) {
            MDC.put(CLUSTER_ID_MDC_KEY, clusterId.getId());
        }
    }

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
        clearMdcKeys();
        extractClusterId(containerRequest);

        return containerRequest;
    }
}
