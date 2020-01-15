package se.tink.backend.integration.tpp_secrets_service.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslX509KeyManagerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;
import se.tink.backend.secretservice.grpc.GetAllSecretsResponse;
import se.tink.backend.secretservice.grpc.GetSecretsRequest;
import se.tink.backend.secretservice.grpc.InternalSecretsServiceGrpc;
import se.tink.backend.secretservice.grpc.PingMessage;
import se.tink.backend.secretservice.grpc.TppSecret;

public final class TppSecretsServiceClientImpl implements ManagedTppSecretsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TppSecretsServiceClientImpl.class);
    private final AtomicBoolean isShutDown = new AtomicBoolean(false);

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private final String LOCAL_CLIENT_P12_FILE_PASS = "changeme";
    private InternalSecretsServiceGrpc.InternalSecretsServiceBlockingStub
            internalSecretsServiceStub;
    private final TppSecretsServiceConfiguration tppSecretsServiceConfiguration;
    private ManagedChannel channel;
    private final boolean enabled;
    private final SslContext sslContext;

    @Inject
    public TppSecretsServiceClientImpl(
            TppSecretsServiceConfiguration tppSecretsServiceConfiguration) {
        Preconditions.checkNotNull(
                tppSecretsServiceConfiguration, "tppSecretsServiceConfiguration not found.");

        this.tppSecretsServiceConfiguration = tppSecretsServiceConfiguration;
        this.enabled = tppSecretsServiceConfiguration.isEnabled();
        sslContext = buildSslContext();
    }

    @Override
    public void start() {
        if (enabled) {
            this.channel =
                    NettyChannelBuilder.forAddress(
                                    tppSecretsServiceConfiguration.getHost(),
                                    tppSecretsServiceConfiguration.getPort())
                            .useTransportSecurity()
                            .sslContext(sslContext)
                            .build();

            internalSecretsServiceStub = InternalSecretsServiceGrpc.newBlockingStub(channel);
            log.info("Connection re-establish mechanism activates");
            this.channel.notifyWhenStateChanged(ConnectivityState.IDLE, this::reconnectIfNecessary);
        } else {
            log.warn(
                    "Trying to start an instance of TppSecretsServiceClientImpl when the configuration says it is not enabled.");
        }
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

        if (!enabled) {
            log.warn(
                    "Trying to call getAllSecrets for an instance of TppSecretsServiceClientImpl when the configuration says it is not enabled.");
            return Optional.empty();
        }

        // TODO: Remove this once Access team confirms there are no null appIds
        if (Strings.emptyToNull(appId) == null
                || Strings.emptyToNull(financialInstitutionId) == null) {
            return Optional.empty();
        }

        GetSecretsRequest getSecretsRequest =
                buildRequest(financialInstitutionId, appId, clusterId);

        GetAllSecretsResponse response =
                internalSecretsServiceStub.getAllSecrets(getSecretsRequest);

        List<TppSecret> allSecretsList = new ArrayList<>();
        allSecretsList.addAll(response.getEncryptedSecretsList());
        allSecretsList.addAll(response.getSecretsList());

        return Optional.of(
                new SecretsEntityCore.Builder()
                        .setSecrets(
                                allSecretsList.stream()
                                        .collect(
                                                Collectors.toMap(
                                                        TppSecret::getKey, TppSecret::getValue)))
                        .setRedirectUrls(response.getRedirectUrlsList())
                        .setScopes(response.getScopesList())
                        .build());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private GetSecretsRequest buildRequest(
            String financialInstitutionId, String appId, String clusterId) {
        Preconditions.checkNotNull(
                financialInstitutionId, "financialInstitutionId must not be null");
        Preconditions.checkNotNull(appId, "appId must not be null");
        Preconditions.checkNotNull(clusterId, "clusterId must not be null");

        return GetSecretsRequest.newBuilder()
                .setFinancialInstitutionId(financialInstitutionId)
                .setAppId(appId)
                .setClusterId(clusterId)
                .build();
    }

    private SslContext buildSslContext() {
        File caCertPath = getCaCertPath(tppSecretsServiceConfiguration);
        SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient().trustManager(caCertPath);

        switch (tppSecretsServiceConfiguration.getCertificatesLocation()) {
            case CLUSTER:
                addClusterKeyManager(sslContextBuilder);
                break;

            case DEVELOPMENT_STAGING:
                addHomeDevelopmentStagingKeyManager(sslContextBuilder);
                break;

            case DEVELOPMENT_LOCAL:
                addHomeDevelopmentLocalKeyManager(sslContextBuilder);
                break;

            default:
                throw new IllegalStateException(
                        "Client certificate for TPP Secrets Service not configured");
        }

        try {
            return sslContextBuilder.build();
        } catch (SSLException e) {
            throw new IllegalStateException(
                    "Unexpected error when building SSLContext for TPP Secrets Service client", e);
        }
    }

    private void addHomeDevelopmentLocalKeyManager(SslContextBuilder sslContextBuilder) {
        File localClientCertFile =
                new File(System.getProperty("user.home"), "/.eidas/local-cluster/ss/tls.crt");
        File localClientKeyFile =
                new File(System.getProperty("user.home"), "/.eidas/local-cluster/ss/tls.key");
        if (!localClientCertFile.exists() || !localClientKeyFile.exists()) {
            throw getLocalClusterTlsMaFilesNotAvailableException();
        }
        sslContextBuilder.keyManager(localClientCertFile, localClientKeyFile);
    }

    private void addHomeDevelopmentStagingKeyManager(SslContextBuilder sslContextBuilder) {
        File clientP12File = new File(System.getProperty("user.home"), "/.eidas/eidas_client.p12");

        try {
            ByteArrayInputStream clientCertificateStream =
                    new ByteArrayInputStream(Files.toByteArray(clientP12File));

            KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(clientCertificateStream, LOCAL_CLIENT_P12_FILE_PASS.toCharArray());

            final KeyManagerFactory keyManagerFactory;
            if (OpenSsl.supportsKeyManagerFactory()) {
                keyManagerFactory = new OpenSslX509KeyManagerFactory();
            } else {
                keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            }
            keyManagerFactory.init(keyStore, LOCAL_CLIENT_P12_FILE_PASS.toCharArray());

            sslContextBuilder.keyManager(keyManagerFactory);
        } catch (KeyStoreException
                | NoSuchProviderException
                | IOException
                | NoSuchAlgorithmException
                | CertificateException
                | UnrecoverableKeyException e) {
            throw new IllegalStateException(
                    "Problem encountered when setting up client "
                            + "authentication to Secrets Service running in staging environment",
                    e);
        }
    }

    private void addClusterKeyManager(SslContextBuilder sslContextBuilder) {
        if (tppSecretsServiceConfiguration.getTlsCrtPath() != null
                && tppSecretsServiceConfiguration.getTlsKeyPath() != null) {
            sslContextBuilder.keyManager(
                    new File(tppSecretsServiceConfiguration.getTlsCrtPath()),
                    new File(tppSecretsServiceConfiguration.getTlsKeyPath()));
        } else {
            throw new IllegalStateException(
                    "Missing client authentication key and/or certificate for inside "
                            + "cluster operation of Secrets Service");
        }
    }

    private File getCaCertPath(TppSecretsServiceConfiguration configuration) {
        switch (configuration.getCertificatesLocation()) {
            case CLUSTER:
                if (configuration.getCaPath() != null) {
                    return new File(configuration.getCaPath());
                } else {
                    throw new IllegalStateException(
                            "Missing server certificate for inside cluster operation of Secrets "
                                    + "Service");
                }

            case DEVELOPMENT_STAGING:
                return new File("data/eidas_dev_certificates/aggregation-staging-ca.pem");

            case DEVELOPMENT_LOCAL:
                File localCaCertFile =
                        new File(
                                System.getProperty("user.home"), "/.eidas/local-cluster/ss/ca.crt");
                if (!localCaCertFile.exists()) {
                    throw getLocalClusterTlsMaFilesNotAvailableException();
                }
                return localCaCertFile;

            default:
                throw new IllegalStateException(
                        "Trusted CA for Tpp Secrets Service not configured");
        }
    }

    private IllegalStateException getLocalClusterTlsMaFilesNotAvailableException() {
        return new IllegalStateException(
                "When running a local cluster, store the server and client certificates under "
                        + System.getProperty("user.home")
                        + "/.eidas/local-cluster/ss/ with the following names: ca.crt, tls.key, tls.crt");
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
