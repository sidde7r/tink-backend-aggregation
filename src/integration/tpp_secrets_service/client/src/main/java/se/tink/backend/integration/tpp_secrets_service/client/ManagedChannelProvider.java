package se.tink.backend.integration.tpp_secrets_service.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import java.util.Map;

class ManagedChannelProvider {

    private final String host;
    private final int port;
    private final SslContext sslContext;
    private final boolean enabledRetryPolicy;

    private static Map<String, ?> getServiceConfig() {
        return ImmutableMap.of(
                "methodConfig",
                ImmutableList.of(
                        ImmutableMap.of(
                                "name",
                                ImmutableList.of(
                                        ImmutableMap.of(
                                                "service", "InternalSecretsService",
                                                "method", "GetAllSecrets")),
                                "retryPolicy",
                                ImmutableMap.of(
                                        "maxAttempts",
                                        5.0,
                                        "initialBackoff",
                                        "0.1s",
                                        "maxBackoff",
                                        "1s",
                                        "backoffMultiplier",
                                        1.2,
                                        "retryableStatusCodes",
                                        ImmutableList.of("UNAVAILABLE")))));
    }

    public ManagedChannelProvider(
            String host, int port, SslContext sslContext, boolean enabledRetryPolicy) {
        this.host = host;
        this.port = port;
        this.sslContext = sslContext;
        this.enabledRetryPolicy = enabledRetryPolicy;
    }

    public ManagedChannel getManagedChannel() {
        final NettyChannelBuilder channelBuilder =
                NettyChannelBuilder.forAddress(host, port)
                        .useTransportSecurity()
                        .sslContext(sslContext);

        if (enabledRetryPolicy) {
            channelBuilder
                    .enableRetry()
                    .maxRetryAttempts(5)
                    .defaultServiceConfig(getServiceConfig());
        }

        return channelBuilder.build();
    }
}
