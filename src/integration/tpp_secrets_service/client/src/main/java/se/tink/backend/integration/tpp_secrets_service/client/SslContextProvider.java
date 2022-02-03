package se.tink.backend.integration.tpp_secrets_service.client;

import com.google.common.io.Files;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.OpenSslX509KeyManagerFactory;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;

class SslContextProvider {

    private final String LOCAL_CLIENT_P12_FILE_PASS = "changeme";

    private final TppSecretsServiceConfiguration tppSecretsServiceConfiguration;

    public SslContextProvider(TppSecretsServiceConfiguration tppSecretsServiceConfiguration) {
        this.tppSecretsServiceConfiguration = tppSecretsServiceConfiguration;
    }

    public SslContext getSslContext() {
        File caCertPath = getCaCertPath(tppSecretsServiceConfiguration);
        io.netty.handler.ssl.SslContextBuilder sslContextBuilder =
                GrpcSslContexts.forClient().trustManager(caCertPath);

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
            sslContextBuilder = GrpcSslContexts.configure(sslContextBuilder, SslProvider.OPENSSL);
            return sslContextBuilder.build();
        } catch (SSLException e) {
            throw new IllegalStateException(
                    "Unexpected error when building SSLContext for TPP Secrets Service client", e);
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

    private void addClusterKeyManager(io.netty.handler.ssl.SslContextBuilder sslContextBuilder) {
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

    private void addHomeDevelopmentStagingKeyManager(
            io.netty.handler.ssl.SslContextBuilder sslContextBuilder) {
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

    private void addHomeDevelopmentLocalKeyManager(
            io.netty.handler.ssl.SslContextBuilder sslContextBuilder) {
        File localClientCertFile =
                new File(System.getProperty("user.home"), "/.eidas/local-cluster/ss/tls.crt");
        File localClientKeyFile =
                new File(System.getProperty("user.home"), "/.eidas/local-cluster/ss/tls.key");
        if (!localClientCertFile.exists() || !localClientKeyFile.exists()) {
            throw getLocalClusterTlsMaFilesNotAvailableException();
        }
        sslContextBuilder.keyManager(localClientCertFile, localClientKeyFile);
    }
}
