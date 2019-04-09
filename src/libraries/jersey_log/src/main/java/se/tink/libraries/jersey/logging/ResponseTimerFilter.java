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
import se.tink.libraries.metrics.Timer;

class ResponseTimerFilter
        implements ContainerRequestFilter, ContainerResponseFilter, ResourceFilter {

    private static final Logger log = LoggerFactory.getLogger(ResponseTimerFilter.class);
    private static final String timerContextKey = "timerContext";
    private final MetricRegistry metricRegistry;
    private final String path;
    private final Optional<TeamOwnership> teamOwnership;

    ResponseTimerFilter(
            MetricRegistry metricRegistry, String path, Optional<TeamOwnership> teamOwnership) {
        this.metricRegistry = metricRegistry;
        this.path = path;
        this.teamOwnership = teamOwnership;
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        if (request.getProperties().containsKey(timerContextKey)) {
            log.warn("Filter already applied to request");
        } else {
            Timer.Context timerContext = new Timer.Context();
            request.getProperties().put(timerContextKey, timerContext);
        }
        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        Object maybeTimerContext = request.getProperties().get(timerContextKey);
        if (maybeTimerContext instanceof Timer.Context) {
            Timer.Context timerContext = (Timer.Context) maybeTimerContext;
            MetricId metricId =
                    MetricId.newId("api_response_duration")
                            .label("method", request.getMethod())
                            .label("path", this.path)
                            .label("status", Integer.toString(response.getStatus()))
                            .label(
                                    "team_owner",
                                    teamOwnership
                                            .map(TeamOwnership::value)
                                            .map(Enum::name)
                                            .orElse(""));
            this.metricRegistry.timer(metricId).time(timerContext).stop();
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
