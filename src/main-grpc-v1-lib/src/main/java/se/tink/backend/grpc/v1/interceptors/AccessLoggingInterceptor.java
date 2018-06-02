package se.tink.backend.grpc.v1.interceptors;

import com.google.common.base.Stopwatch;
import io.grpc.ForwardingServerCall;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.grpc.v1.utils.TinkGrpcHeaders;
import se.tink.backend.utils.RemoteAddressUtils;
import se.tink.libraries.access_logging.AccessLoggingRequestDetails;
import se.tink.libraries.access_logging.AccessLoggingUtils;

public class AccessLoggingInterceptor implements ServerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AccessLoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        ServerCall<ReqT, RespT> forwardingServerCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(
                serverCall) {
            @Override
            public void close(Status status, Metadata outMetadata) {
                try {
                    logRequest(getRequestDetails(serverCall, stopwatch.elapsed(TimeUnit.MILLISECONDS), metadata,
                            status.getCode().toString()));
                } catch (Exception e) {
                    log.error("Cannot log request: " + serverCall.getMethodDescriptor().getFullMethodName(), e);
                }
                super.close(status, outMetadata);
            }
        };
        return serverCallHandler.startCall(forwardingServerCall, metadata);
    }

    private void logRequest(AccessLoggingRequestDetails requestDetails) {
        AccessLoggingUtils.log(requestDetails);
    }

    private <ReqT, RespT> AccessLoggingRequestDetails getRequestDetails(ServerCall<ReqT, RespT> serverCall,
            Long responseTime, Metadata metadata, String status) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        if (authenticationContext != null) {
            return AccessLoggingRequestDetails.builder()
                    .setUserId(authenticationContext.isAuthenticated() ? authenticationContext.getUser().getId() : null)
                    .setRequestString(serverCall.getMethodDescriptor().getFullMethodName())
                    .setResponseTimeString(responseTime.toString())
                    .setHttpAuthenticationMethod(authenticationContext.isAuthenticated() ?
                            authenticationContext.getHttpAuthenticationMethod() : null)
                    .setRemoteHost(authenticationContext.getRemoteAddress().orElse(null))
                    .setUserAgent(authenticationContext.getUserAgent().orElse(null))
                    .setResponseStatus(status)
                    .build();
        }

        SocketAddress socketAddress = serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);

        return AccessLoggingRequestDetails.builder()
                .setRequestString(serverCall.getMethodDescriptor().getFullMethodName())
                .setResponseTimeString(responseTime.toString())
                .setRemoteHost(RemoteAddressUtils.getRemoteAddress(socketAddress))
                .setUserAgent(metadata.get(TinkGrpcHeaders.USER_AGENT))
                .setResponseStatus(status)
                .build();
    }
}

