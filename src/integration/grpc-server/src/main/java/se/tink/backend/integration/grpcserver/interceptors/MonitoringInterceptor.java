package se.tink.backend.integration.gprcserver.interceptors;

import com.google.inject.Inject;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.lang.reflect.Method;
import java.util.Optional;
import se.tink.backend.integration.gprcserver.interceptors.metrics.GrpcMethod;
import se.tink.backend.integration.gprcserver.interceptors.metrics.GrpcMethodUtils;
import se.tink.backend.integration.gprcserver.interceptors.metrics.MonitoringCall;
import se.tink.backend.integration.gprcserver.interceptors.metrics.MonitoringCallListener;
import se.tink.libraries.api.annotations.TeamOwnership;
import se.tink.libraries.metrics.MetricRegistry;

public class MonitoringInterceptor implements ServerInterceptor {
    private final MetricRegistry metricRegistry;

    @Inject
    MonitoringInterceptor(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public <R, S> ServerCall.Listener<R> interceptCall(
            ServerCall<R, S> call, Metadata requestHeaders, ServerCallHandler<R, S> next) {
        MethodDescriptor<R, S> method = call.getMethodDescriptor();
        GrpcMethod grpcMethod = GrpcMethod.of(method);

        // Get the implementation metod in the gRPC service
        Optional<Method> calledMethod = GrpcMethodUtils.getCalledMethod(call, next);
        TeamOwnership team =
                calledMethod
                        .map(method2 -> method2.getAnnotation(TeamOwnership.class))
                        .orElse(null);

        ServerCall<R, S> monitoringCall =
                new MonitoringCall(call, grpcMethod, team, metricRegistry);

        return new MonitoringCallListener<>(
                next.startCall(monitoringCall, requestHeaders), grpcMethod, team, metricRegistry);
    }
}
