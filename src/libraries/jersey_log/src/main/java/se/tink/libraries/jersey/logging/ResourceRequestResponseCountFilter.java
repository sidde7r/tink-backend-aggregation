package se.tink.libraries.jersey.logging;



import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.api.annotations.TeamOwnership;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class ResourceRequestResponseCountFilter
        implements ContainerRequestFilter, ContainerResponseFilter, ResourceFilter {

    private static final String REQUEST_METRIC_ID_NAME = "api_http_requests";
    private static final String RESPONSE_METRIC_ID_NAME = "api_http_responses";
    private static final Logger log = LoggerFactory.getLogger(ResourceRequestResponseCountFilter.class);
    private static final String requestCounterKey = "requestCounter";
    private final String path;
    private final Optional<TeamOwnership> teamOwnership;
    private final MetricRegistry metricRegistry;

    ResourceRequestResponseCountFilter(
            MetricRegistry metricRegistry, String path, Optional<TeamOwnership> teamOwnership) {
        this.metricRegistry = metricRegistry;
        this.path = path;
        this.teamOwnership = teamOwnership;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        if (request.getProperties().containsKey(requestCounterKey)) {
            log.warn("Filter already applied to request");
        } else {
            MetricId requestMetricId =
                    MetricId.newId(REQUEST_METRIC_ID_NAME)
                            .label("method", request.getMethod())
                            .label("path", this.path)
                            .label(
                                    "team_owner",
                                    teamOwnership
                                            .map(TeamOwnership::value)
                                            .map(Enum::name)
                                            .orElse(""));
            this.metricRegistry.meter(requestMetricId).inc();
            request.getProperties().put(requestCounterKey, "");
        }
        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        if (request.getProperties().containsKey(requestCounterKey)) {
            MetricId responseMetricId =
                    MetricId.newId(RESPONSE_METRIC_ID_NAME)
                            .label("method", request.getMethod())
                            .label("path", this.path)
                            .label("status", Integer.toString(response.getStatus()))
                            .label(
                                    "team_owner",
                                    teamOwnership
                                            .map(TeamOwnership::value)
                                            .map(Enum::name)
                                            .orElse(""));
            this.metricRegistry.meter(responseMetricId).inc();
        }
        return response;
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
        return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
        return this;
    }
}
