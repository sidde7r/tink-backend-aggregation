package se.tink.backend.integration.agent_data_availability_tracker.module;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import java.io.File;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.agent_data_availability_tracker.common.configuration.AgentDataAvailabilityTrackerConfiguration;

public final class TlsChannelProvider implements Provider<ManagedChannel> {

    private static final Logger log = LoggerFactory.getLogger(TlsChannelProvider.class);

    private final String host;
    private final int port;
    private final String caPath;

    @Inject
    private TlsChannelProvider(final AgentDataAvailabilityTrackerConfiguration configuration) {
        this.host = Preconditions.checkNotNull(configuration.getHost());
        this.port = configuration.getPort();
        this.caPath = Preconditions.checkNotNull(configuration.getCaPath());
    }

    @Override
    public ManagedChannel get() {
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port);

        SslContext sslContext = null;
        try {
            sslContext = GrpcSslContexts.forClient().trustManager(new File(caPath)).build();
        } catch (SSLException e) {
            throw new ProvisionException(e.getMessage());
        }

        channelBuilder.useTransportSecurity().sslContext(sslContext).build();

        log.debug(String.format("Opening connection: %s:%d", host, port));

        return channelBuilder.build();
    }
}
