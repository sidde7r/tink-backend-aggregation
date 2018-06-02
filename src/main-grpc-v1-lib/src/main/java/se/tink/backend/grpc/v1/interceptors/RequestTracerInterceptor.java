package se.tink.backend.grpc.v1.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.util.Optional;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.libraries.request_tracing.RequestTracer;

public class RequestTracerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        RequestTracer.startTracing(Optional.ofNullable(metadata.get(TinkGrpcHeaders.REQUEST_ID)));
        return serverCallHandler.startCall(serverCall, metadata);
    }
}
