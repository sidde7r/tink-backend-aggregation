package se.tink.backend.integration.tpp_secrets_service.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.secretservice.grpc.GetAllSecretsResponse;
import se.tink.backend.secretservice.grpc.GetSecretsRequest;
import se.tink.backend.secretservice.grpc.InternalSecretsServiceGrpc;
import se.tink.backend.secretservice.grpc.TppSecret;

public final class TppSecretsServiceClientImpl implements TppSecretsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TppSecretsServiceClientImpl.class);

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private final String LOCAL_CLIENT_P12_FILE_PASS = "changeme";
    private InternalSecretsServiceGrpc.InternalSecretsServiceBlockingStub
            internalSecretsServiceStub;
    private final TppSecretsServiceConfiguration tppSecretsServiceConfiguration;
    private ManagedChannel channel;
    private final boolean enabled;

    @Inject
    public TppSecretsServiceClientImpl(
            TppSecretsServiceConfiguration tppSecretsServiceConfiguration) {
        Preconditions.checkNotNull(
                tppSecretsServiceConfiguration, "tppSecretsServiceConfiguration not found.");

        this.tppSecretsServiceConfiguration = tppSecretsServiceConfiguration;
        this.enabled = tppSecretsServiceConfiguration.isEnabled();
    }

    @Override
    public void start() {
        if (enabled) {
            SslContext sslContext = buildSslContext();

            this.channel =
                    NettyChannelBuilder.forAddress(
                                    tppSecretsServiceConfiguration.getHost(),
                                    tppSecretsServiceConfiguration.getPort())
                            .useTransportSecurity()
                            .sslContext(sslContext)
                            .build();

            internalSecretsServiceStub = InternalSecretsServiceGrpc.newBlockingStub(channel);
        } else {
            log.warn(
                    "Trying to start an instance of TppSecretsServiceClientImpl when the configuration says it is not enabled.");
        }
    }

    @Override
    public void stop() {
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
    public Optional<Map<String, String>> getAllSecrets(
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

        GetAllSecretsResponse allSecretsResponse =
                internalSecretsServiceStub.getAllSecrets(getSecretsRequest);

        List<TppSecret> allSecretsList = new ArrayList<>();
        allSecretsList.addAll(allSecretsResponse.getEncryptedSecretsList());
        allSecretsList.addAll(allSecretsResponse.getSecretsList());

        return Optional.of(
                allSecretsList.stream()
                        .collect(Collectors.toMap(TppSecret::getKey, TppSecret::getValue)));
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
}
