package se.tink.backend.integration.agent_data_availability_tracker.client.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import java.net.InetSocketAddress;
import org.junit.Ignore;

@Ignore
public final class PlaintextChannelProvider implements Provider<ManagedChannel> {

    private final InetSocketAddress socket;

    @Inject
    private PlaintextChannelProvider(final InetSocketAddress socket) {
        this.socket = socket;
    }

    @Override
    public ManagedChannel get() {
        return NettyChannelBuilder.forAddress(socket.getHostName(), socket.getPort())
                .usePlaintext()
                .build();
    }
}
