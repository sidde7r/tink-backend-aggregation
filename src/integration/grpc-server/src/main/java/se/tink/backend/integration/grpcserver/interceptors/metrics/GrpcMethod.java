package se.tink.backend.integration.gprcserver.interceptors.metrics;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;

public class GrpcMethod {
    private final String serviceName;
    private final String methodName;
    private final MethodType type;

    public static GrpcMethod of(MethodDescriptor<?, ?> method) {
        String serviceName = MethodDescriptor.extractFullServiceName(method.getFullMethodName());

        // Full method names are of the form: "full.serviceName/MethodName". We extract the last
        // part.
        String methodName = method.getFullMethodName().substring(serviceName.length() + 1);
        return new GrpcMethod(serviceName, methodName, method.getType());
    }

    private GrpcMethod(String serviceName, String methodName, MethodType type) {
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.type = type;
    }

    public String serviceName() {
        return serviceName;
    }

    public String methodName() {
        return methodName;
    }

    public String type() {
        return type.toString();
    }

    public boolean streamsRequests() {
        return type == MethodType.CLIENT_STREAMING || type == MethodType.BIDI_STREAMING;
    }

    public boolean streamsResponses() {
        return type == MethodType.SERVER_STREAMING || type == MethodType.BIDI_STREAMING;
    }
}
