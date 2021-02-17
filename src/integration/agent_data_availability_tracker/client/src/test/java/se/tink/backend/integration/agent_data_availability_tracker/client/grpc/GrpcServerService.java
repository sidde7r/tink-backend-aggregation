package se.tink.backend.integration.agent_data_availability_tracker.client.grpc;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import org.junit.Ignore;

@Ignore
public final class GrpcServerService {

    private final Server server;

    GrpcServerService(final CompletableFuture<String> future) {
        this.server =
                NettyServerBuilder.forAddress(new InetSocketAddress(0))
                        .addService(new AgentDataAvailabilityTrackerTestService(future))
                        .build();
    }

    void start() throws IOException {
        server.start();
    }

    int getPort() {
        return server.getPort();
    }
}
