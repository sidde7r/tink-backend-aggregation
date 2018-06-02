package se.tink.backend.grpc.v1.interceptors.metrics;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;
import se.tink.backend.grpc.v1.internal.GrpcMethod;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class MonitoringCallListener <R> extends ForwardingServerCallListener<R> {
    private final ServerCall.Listener<R> delegate;
    private final GrpcMethod grpcMethod;
    private final MetricRegistry metricRegistry;

    public MonitoringCallListener(ServerCall.Listener<R> delegate, GrpcMethod grpcMethod, MetricRegistry metricRegistry) {
        this.delegate = delegate;
        this.grpcMethod = grpcMethod;
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
        MetricId streamRequestCounter = MetricId.newId("grpc_api_stream_requests")
                .label("service", grpcMethod.serviceName())
                .label("method", grpcMethod.methodName());
        metricRegistry.meter(streamRequestCounter).inc();
    }
}
