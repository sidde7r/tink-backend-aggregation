package se.tink.backend.grpc.v1.interceptors.metrics;

import com.google.inject.Inject;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import se.tink.backend.grpc.v1.internal.GrpcMethod;
import se.tink.libraries.metrics.MetricRegistry;

import java.time.Clock;

public class MonitoringInterceptor implements ServerInterceptor {

    private MetricRegistry metricRegistry;

    @Inject
    public MonitoringInterceptor(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public <R, S> ServerCall.Listener<R> interceptCall(ServerCall<R, S> call, Metadata requestHeaders, ServerCallHandler<R, S> next) {
        MethodDescriptor<R, S> method = call.getMethodDescriptor();
        GrpcMethod grpcMethod = GrpcMethod.of(method);
        ServerCall<R,S> monitoringCall = new MonitoringCall(call, grpcMethod, metricRegistry);
        return new MonitoringCallListener<>(next.startCall(monitoringCall, requestHeaders), GrpcMethod.of(method), metricRegistry);
    }

}
