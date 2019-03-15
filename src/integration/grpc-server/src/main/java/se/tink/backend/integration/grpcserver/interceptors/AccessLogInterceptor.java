package se.tink.backend.integration.gprcserver.interceptors;

import com.google.common.base.Stopwatch;
import io.grpc.*;
import io.prometheus.client.Counter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;
import se.tink.backend.integration.gprcserver.GrpcServer;

public class AccessLogInterceptor implements ServerInterceptor {

    private static final Counter requests = Counter.build()
            .namespace("tink_integration")
            .subsystem("grpc")
            .name("requests_total")
            .help("Total requests.")
            .register();

    private static final Logger logger = LogManager.getLogger(GrpcServer.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        ServerCall<ReqT, RespT> forwardingServerCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(
                serverCall) {
            @Override
            public void close(Status status, Metadata outMetadata) {
                requests.inc();

                logger.debug(String.format("Call to %s took %d milliseconds",
                        serverCall.getMethodDescriptor().getFullMethodName(),
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));

                super.close(status, outMetadata);
            }
        };
        return serverCallHandler.startCall(forwardingServerCall, metadata);
    }
}
