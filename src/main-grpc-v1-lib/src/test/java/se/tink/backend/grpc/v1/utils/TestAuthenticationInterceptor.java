package se.tink.backend.grpc.v1.utils;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.auth.DefaultAuthenticationContext;

public class TestAuthenticationInterceptor implements ServerInterceptor {

    private final DefaultAuthenticationContext authenticationContext;

    public TestAuthenticationInterceptor(DefaultAuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Context context = Context.current().withValue(AuthenticationInterceptor.CONTEXT, authenticationContext);
        return Contexts.interceptCall(context, serverCall, metadata, serverCallHandler);
    }
}
