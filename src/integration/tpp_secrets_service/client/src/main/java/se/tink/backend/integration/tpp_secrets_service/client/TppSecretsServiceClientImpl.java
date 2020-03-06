package se.tink.backend.integration.tpp_secrets_service.client;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import java.security.Security;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;
import se.tink.backend.secretservice.grpc.InternalSecretsServiceGrpc;
import se.tink.backend.secretservice.grpc.PingMessage;

public final class TppSecretsServiceClientImpl implements ManagedTppSecretsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TppSecretsServiceClientImpl.class);
    private final AtomicBoolean isShutDown = new AtomicBoolean(false);

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private InternalSecretsServiceGrpc.InternalSecretsServiceBlockingStub
            internalSecretsServiceStub;
    private final TppSecretsServiceConfiguration tppSecretsServiceConfiguration;
    private ManagedChannel channel;
    private final boolean enabled;
    private SslContext sslContext;
    private final boolean enabledRetryPolicy;

    @Inject
    public TppSecretsServiceClientImpl(
            TppSecretsServiceConfiguration tppSecretsServiceConfiguration) {
        Preconditions.checkNotNull(
                tppSecretsServiceConfiguration, "tppSecretsServiceConfiguration not found.");

        this.tppSecretsServiceConfiguration = tppSecretsServiceConfiguration;
        this.enabled = tppSecretsServiceConfiguration.isEnabled();
        if (this.enabled) {
            sslContext = buildSslContext();
        }
        enabledRetryPolicy = tppSecretsServiceConfiguration.isEnabledRetryPolicy();
    }

    @Override
    public void start() {
        if (enabled) {
            this.channel = buildChannel();

            internalSecretsServiceStub = InternalSecretsServiceGrpc.newBlockingStub(channel);
            log.info("Connection re-establish mechanism activates");
            this.channel.notifyWhenStateChanged(ConnectivityState.IDLE, this::reconnectIfNecessary);
        } else {
            log.warn(
                    "Trying to start an instance of TppSecretsServiceClientImpl when the configuration says it is not enabled.");
        }
    }

    private ManagedChannel buildChannel() {
        final ManagedChannelProvider managedChannelProvider =
                new ManagedChannelProvider(
                        tppSecretsServiceConfiguration.getHost(),
                        tppSecretsServiceConfiguration.getPort(),
                        sslContext,
                        enabledRetryPolicy);
        return managedChannelProvider.getManagedChannel();
    }

    @Override
    public synchronized void stop() {
        isShutDown.set(true);
        if (enabled) {
            if (channel == null) {
                log.warn(
                        "Trying to shutdown the channel in an instance of TppSecretsServiceClientImpl where it wasn't instantiated.");
            } else {
                try {
                    channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.warn(
                            "TppSecretsServiceClient channel was not able to shutdown gracefully.");
                }
            }
        } else {
            log.warn(
                    "Trying to stop an instance of TppSecretsServiceClientImpl when the configuration says it is not enabled.");
        }
    }

    @Override
    public Optional<SecretsEntityCore> getAllSecrets(
            String financialInstitutionId, String appId, String clusterId) {
        final AllSecretsFetcher allSecretsFetcher =
                new AllSecretsFetcher(enabled, internalSecretsServiceStub);
        return allSecretsFetcher.getAllSecrets(financialInstitutionId, appId, clusterId);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private SslContext buildSslContext() {
        final SslContextProvider sslContextProvider =
                new SslContextProvider(tppSecretsServiceConfiguration);
        return sslContextProvider.getSslContext();
    }

    private synchronized void reconnectIfNecessary() {
        if (!isShutDown.get()) {
            this.channel.notifyWhenStateChanged(
                    this.channel.getState(false), this::reconnectIfNecessary);
            // Use current state as the source state to avoid recursively running of this method.
            // https://github.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/internal/ConnectivityStateManager.java
            if (this.channel.getState(false) == ConnectivityState.TRANSIENT_FAILURE
                    || channel.isShutdown()
                    || channel.isTerminated()) {
                log.info(
                        "Secrets service client reconnect due to state changed to TRANSIENT_FAILURE");
                this.channel.resetConnectBackoff();
            } else if (this.channel.getState(false) == ConnectivityState.IDLE) {
                try {
                    this.internalSecretsServiceStub.ping(PingMessage.newBuilder().build());
                } catch (Exception e) {
                    log.info(
                            "Secrets service client reconnect due to ping failed {} in IDLE state",
                            e.getMessage());
                    this.channel.resetConnectBackoff();
                }
            } else if (this.channel.getState(false) == ConnectivityState.SHUTDOWN) {
                try {
                    if (tppSecretsServiceConfiguration != null && sslContext != null) {

                        ManagedChannel newChannel =
                                NettyChannelBuilder.forAddress(
                                                tppSecretsServiceConfiguration.getHost(),
                                                tppSecretsServiceConfiguration.getPort())
                                        .useTransportSecurity()
                                        .sslContext(sslContext)
                                        .build();
                        newChannel.notifyWhenStateChanged(
                                this.channel.getState(false), this::reconnectIfNecessary);
                        this.channel = newChannel;
                        internalSecretsServiceStub =
                                InternalSecretsServiceGrpc.newBlockingStub(channel);
                    }
                } catch (Exception e) {
                    log.info(
                            "Secrets service client reconnect due to ping failed {} in shutdown state",
                            e.getMessage());
                    this.channel.resetConnectBackoff();
                }
            }
        }
    }
}
