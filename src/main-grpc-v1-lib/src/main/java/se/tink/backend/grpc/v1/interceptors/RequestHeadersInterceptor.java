package se.tink.backend.grpc.v1.interceptors;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class RequestHeadersInterceptor implements ServerInterceptor {
    public static final Context.Key<Metadata> HEADERS = Context.key("headers");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Context context = Context.current().withValue(HEADERS, metadata);
        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
}
