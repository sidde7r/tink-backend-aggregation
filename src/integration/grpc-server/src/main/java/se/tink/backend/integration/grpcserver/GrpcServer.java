package se.tink.backend.integration.gprcserver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GrpcServer {
    private Server server;
    private static final Logger logger = LogManager.getLogger(GrpcServer.class);

    public GrpcServer(List<? extends BindableService> services, SocketAddress listenAddress) {
        NettyServerBuilder serverBuilder = NettyServerBuilder
                .forAddress(listenAddress);

        List<? extends ServerInterceptor> interceptors = ImmutableList.of(
                new AccessLogInterceptor()
        );

        services.stream().map(s -> ServerInterceptors.intercept(s, Lists.newArrayList(interceptors)))
                .forEach(serverBuilder::addService);

        this.server = serverBuilder.build();
    }

    public void start() throws IOException, InterruptedException {
        this.server.start();
        logger.debug("Started gRPC server on port " + this.server.getPort());
    }

    public void stop(final CountDownLatch shutdownLatch, int duration, TimeUnit unit) throws InterruptedException {
        Runnable shutdownThread = () -> {
            try {
                this.server.shutdown();
                this.server.awaitTermination(duration, unit);
            } catch (InterruptedException e) {
                logger.warn("gRPC server was not able to terminate within timeout and was not shutdown gracefully");
            } finally {
                shutdownLatch.countDown();
            }
        };

        shutdownThread.run();
    }
}
