package se.tink.backend.aggregation.grpc;

import com.google.inject.Inject;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcServer {
    private static final Logger log = LoggerFactory.getLogger(GrpcServer.class);
    private final int port;
    private final Set<BindableService> services;
    private Server server;

    @Inject
    public GrpcServer(@Named("gRpcPort") int port, @Named("grpcServices") Set<BindableService> services) {
        this.port = port;
        this.services = services;
    }

    @PostConstruct
    public void start() throws IOException {
        ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
        services.forEach(serverBuilder::addService);
        server = serverBuilder.build();
        server.start();
        log.info("gRPC server started on port {}", port);
    }

    @PreDestroy
    public void shutdown() {
        server.shutdown();
        log.info("gRPC server shut down");
    }
}
