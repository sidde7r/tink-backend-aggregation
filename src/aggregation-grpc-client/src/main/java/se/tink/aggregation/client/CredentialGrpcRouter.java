package se.tink.aggregation.client;

import com.google.common.collect.ImmutableList;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.details.ServiceCacheListener;
import se.tink.aggregation.grpc.CredentialServiceGrpc;

public class CredentialGrpcRouter implements ServiceCacheListener {

    public static final Random random = new Random();

    private static class Endpoint {
        private final String host;
        private final int port;

        private Endpoint(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Endpoint endpoint = (Endpoint) o;
            return port == endpoint.port &&
                    Objects.equals(host, endpoint.host);
        }

        @Override
        public int hashCode() {
            return Objects.hash(host, port);
        }
    }

    private final ServiceCache<?> discoveryCache;
    private final Map<Endpoint, CredentialServiceGrpc.CredentialServiceBlockingStub> channels = new HashMap<>();
    private ImmutableList<CredentialServiceGrpc.CredentialServiceBlockingStub> channelIndex;

    CredentialGrpcRouter(ServiceCache<?> discoveryCache) {
        this.discoveryCache = discoveryCache;
        updateChannels();
    }

    /**
     * Synchronized to avoid race conditions between updates to the instances fetched from the current thread
     * and updates received through the listener during initialization.
     */
    @PostConstruct
    private synchronized void subscribeToServiceDiscovery() {
        discoveryCache.addListener(this);
        // fetch potentially missed updates
        updateChannels();
    }

    private void updateChannels() {
        Set<Endpoint> newEndpoints = discoveryCache.getInstances().stream()
                .map(i -> new Endpoint(i.getAddress(), i.getPort()))
                .collect(Collectors.toSet());
        channels.keySet().retainAll(newEndpoints);
        newEndpoints.forEach(e -> channels.computeIfAbsent(e, this::createChannel));
        channelIndex = ImmutableList.copyOf(channels.values());
    }

    private CredentialServiceGrpc.CredentialServiceBlockingStub createChannel(Endpoint endpoint) {
        return CredentialServiceGrpc.newBlockingStub(ManagedChannelBuilder
                .forAddress(endpoint.host, endpoint.port)
                .usePlaintext(true)
                .build());
    }

    CredentialServiceGrpc.CredentialServiceBlockingStub pickServer() {
        return channelIndex.get(channelIndex());
    }

    private int channelIndex() {
        return random.nextInt(channelIndex.size());
    }

    /**
     * See {@link #subscribeToServiceDiscovery()}.
     */
    @Override
    public synchronized void cacheChanged() {
        updateChannels();
    }

    @Override
    public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
        // no-op
    }

}
