package se.tink.backend.grpc.v1.interceptors.metrics;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import se.tink.backend.grpc.v1.internal.GrpcMethod;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class MonitoringCall<R, S> extends ForwardingServerCall.SimpleForwardingServerCall<R, S> {

    private final GrpcMethod grpcMethod;
    private final MetricRegistry metricRegistry;
    private final Timer.Context timerContext;

    private static final LogUtils log = new LogUtils(MonitoringCall.class);

    public MonitoringCall(ServerCall<R, S> delegate, GrpcMethod grpcMethod, MetricRegistry metricRegistry) {
        super(delegate);
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
            log.warn("Status: " + status.getDescription(), e);
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
        MetricId streamResponseCounter = MetricId.newId("grpc_api_stream_responses")
                .label("service", grpcMethod.serviceName())
                .label("method", grpcMethod.methodName());
        metricRegistry.meter(streamResponseCounter).inc();
    }

    private void reportEndMetrics(Status status) {
        MetricId apiResponseMetric = MetricId.newId("grpc_api_response_time")
                .label("service", grpcMethod.serviceName())
                .label("method", grpcMethod.methodName())
                .label("status", status.getCode().toString());
        metricRegistry.timer(apiResponseMetric).time(timerContext).stop();
    }
}
