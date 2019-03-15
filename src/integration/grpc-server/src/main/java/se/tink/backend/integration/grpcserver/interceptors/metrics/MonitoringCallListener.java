package se.tink.backend.integration.gprcserver.interceptors.metrics;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;
import se.tink.libraries.api.annotations.TeamOwnership;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class MonitoringCallListener<R> extends ForwardingServerCallListener<R> {
    private final ServerCall.Listener<R> delegate;
    private final GrpcMethod grpcMethod;
    private final TeamOwnership teamOwnership;
    private final MetricRegistry metricRegistry;

    public MonitoringCallListener(
            ServerCall.Listener<R> delegate,
            GrpcMethod grpcMethod,
            TeamOwnership teamOwnership,
            MetricRegistry metricRegistry) {
        this.delegate = delegate;
        this.grpcMethod = grpcMethod;
        this.teamOwnership = teamOwnership;
        this.metricRegistry = metricRegistry;
    }

    @Override
    protected ServerCall.Listener<R> delegate() {
        return delegate;
    }

    @Override
    public void onMessage(R request) {
        if (grpcMethod.streamsRequests()) {
            recordStreamRequest();
        }
        super.onMessage(request);
    }

    private void recordStreamRequest() {
        MetricId streamRequestCounter =
                MetricId.newId("grpc_api_stream_requests")
                        .label("service", grpcMethod.serviceName())
                        .label("method", grpcMethod.methodName())
                        .label("team_owner", teamOwnership == null ? "" : teamOwnership.toString());

        metricRegistry.meter(streamRequestCounter).inc();
    }
}
