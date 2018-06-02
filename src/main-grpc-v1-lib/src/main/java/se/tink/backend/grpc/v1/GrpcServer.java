package se.tink.backend.grpc.v1;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLException;

import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.common.config.GrpcConfiguration;

public class GrpcServer {
    private final static Logger log = LoggerFactory.getLogger(GrpcServer.class);
    private final int port;
    private final Server server;

    /**
     * Create a GrpcServer by {@code port} and  {@code services}
     **/
    @Inject
    public GrpcServer(GrpcConfiguration configuration,
            @Named("grpcServices") Set<BindableService> bindableServices,
            @Named("grpcInterceptors") Set<ServerInterceptor> interceptors) throws SSLException {
        this.port = configuration.getPort();
        List<ServerInterceptor> interceptorsList = Lists.newArrayList(interceptors);

        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
        if (configuration.isUseTLS()) {
            log.debug("Is OpenSSL available? " + OpenSsl.isAvailable());
            log.debug("Is ALPN available?    " + OpenSsl.isAlpnSupported());

            SslContextBuilder sslContextBuilder = GrpcSslContexts.forServer(new File(configuration.getCertPath()), new File(configuration.getKeyPath()));
            sslContextBuilder = GrpcSslContexts.configure(sslContextBuilder, SslProvider.OPENSSL);
            serverBuilder.sslContext(sslContextBuilder.build());
            log.info("TLS enabled on the GRPC Api.");
        } else {
            log.info("TLS not enabled on the GRPC Api.");
        }

        serverBuilder
                .keepAliveTime(configuration.getKeepAliveTime(), TimeUnit.SECONDS)
                .keepAliveTimeout(configuration.getKeepAliveTimeout(), TimeUnit.SECONDS)
                .maxConnectionIdle(configuration.getMaxConnectionIdle(), TimeUnit.SECONDS)
                .maxConnectionAge(configuration.getMaxConnectionAge(), TimeUnit.SECONDS);

        // We might need to add something like that for performance reasons.
        // TODO: serverBuilder.executor(Executors.newFixedThreadPool(configuration.getThreads()));


        bindableServices.stream()
                .map(bindableService -> ServerInterceptors.intercept(bindableService, interceptorsList))
                .forEach(serverBuilder::addService);
        server = serverBuilder.build();
    }

    /**
     * Start serving requests.
     */
    @PostConstruct
    public void start() throws IOException {
        server.start();
        log.info("gRPC server started, listening on " + port);
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    @PreDestroy
    public void shutdown() {
        server.shutdown();
    }

}
