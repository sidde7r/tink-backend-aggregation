package se.tink.backend.integration.gprcserver.interceptors.metrics;

import com.google.common.collect.ImmutableList;
import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.tink.libraries.api.annotations.TeamOwnership;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class MonitoringCall<R, S> extends ForwardingServerCall.SimpleForwardingServerCall<R, S> {
    private static final ImmutableList<Double> GRPC_TIMER_BUCKETS =
            ImmutableList.of(0., .01, .025, .05, .1, .25, .5, 1., 5., 10., 20., 30.);
    private static final Logger logger = LogManager.getLogger(MonitoringCall.class);

    private final GrpcMethod grpcMethod;
    private final TeamOwnership teamOwnership;
    private final MetricRegistry metricRegistry;
    private final Timer.Context timerContext;

    public MonitoringCall(
            ServerCall<R, S> delegate,
            GrpcMethod grpcMethod,
            TeamOwnership teamOwnership,
            MetricRegistry metricRegistry) {
        super(delegate);
        this.teamOwnership = teamOwnership;
        this.metricRegistry = metricRegistry;
        this.grpcMethod = grpcMethod;
        timerContext = new Timer.Context();
    }

    @Override
    public void close(Status status, Metadata responseHeaders) {
        reportEndMetrics(status);

        // call was already closed by the exception interceptor, or something similar.
        try {
            if (!super.isCancelled()) {
                super.close(status, responseHeaders);
            }
        } catch (IllegalStateException e) {
            logger.warn("Status: " + status.getDescription(), e);
        }
    }

    @Override
    public void sendMessage(S message) {
        if (grpcMethod.streamsResponses()) {
            recordStreamMetrics();
        }
        super.sendMessage(message);
    }

    private void recordStreamMetrics() {
        MetricId streamResponseCounter =
                MetricId.newId("grpc_api_stream_responses")
                        .label("service", grpcMethod.serviceName())
                        .label("method", grpcMethod.methodName())
                        .label(
                                "team_owner",
                                teamOwnership == null ? "" : teamOwnership.value().toString());

        metricRegistry.meter(streamResponseCounter).inc();
    }

    private void reportEndMetrics(Status status) {
        MetricId apiResponseMetric =
                MetricId.newId("grpc_api_response_time")
                        .label("service", grpcMethod.serviceName())
                        .label("method", grpcMethod.methodName())
                        .label("status", status.getCode().toString())
                        .label(
                                "team_owner",
                                teamOwnership == null ? "" : teamOwnership.value().toString());

        metricRegistry.timer(apiResponseMetric, GRPC_TIMER_BUCKETS).time(timerContext).stop();
    }
}
