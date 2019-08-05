package se.tink.backend.integration.tpp_secrets_service.client;

import com.google.common.io.Files;
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
import java.util.stream.Collectors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.secretservice.grpc.GetAllSecretsResponse;
import se.tink.backend.secretservice.grpc.GetEncryptedSecretsResponse;
import se.tink.backend.secretservice.grpc.GetSecretsRequest;
import se.tink.backend.secretservice.grpc.GetSecretsResponse;
import se.tink.backend.secretservice.grpc.InternalSecretsServiceGrpc;
import se.tink.backend.secretservice.grpc.TppSecret;

public class TppSecretsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TppSecretsServiceClient.class);

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    private final String LOCAL_CLIENT_P12_FILE_PASS = "changeme";
    private final InternalSecretsServiceGrpc.InternalSecretsServiceBlockingStub
            internalSecretsServiceStub;

    public TppSecretsServiceClient(TppSecretsServiceConfiguration configuration) {
        if (configuration.isEnabled()) {
            SslContext sslContext = buildSslContext(configuration);

            ManagedChannel channel =
                    NettyChannelBuilder.forAddress(configuration.getHost(), configuration.getPort())
                            .useTransportSecurity()
                            .sslContext(sslContext)
                            .build();

            internalSecretsServiceStub = InternalSecretsServiceGrpc.newBlockingStub(channel);
        } else {
            log.warn("TPP Secrets Service is not enabled, no client instance will be created.");
            internalSecretsServiceStub = null;
        }
    }

    public Map<String, String> getAllSecrets(String financialInstitutionId, String appId) {
        GetSecretsRequest getSecretsRequest = buildRequest(financialInstitutionId, appId);

        GetAllSecretsResponse allSecretsResponse =
                internalSecretsServiceStub.getAllSecrets(getSecretsRequest);

        List<TppSecret> allSecretsList = new ArrayList<>();
        allSecretsList.addAll(allSecretsResponse.getEncryptedSecretsList());
        allSecretsList.addAll(allSecretsResponse.getSecretsList());

        return allSecretsList.stream()
                .collect(Collectors.toMap(TppSecret::getKey, TppSecret::getValue));
    }

    public Map<String, String> getSecrets(String financialInstitutionId, String appId) {
        GetSecretsRequest getSecretsRequest = buildRequest(financialInstitutionId, appId);

        GetSecretsResponse secretsResponse =
                internalSecretsServiceStub.getSecrets(getSecretsRequest);

        return secretsResponse.getSecretsList().stream()
                .collect(Collectors.toMap(TppSecret::getKey, TppSecret::getValue));
    }

    public Map<String, String> getEncrypted(String financialInstitutionId, String appId) {
        GetSecretsRequest getSecretsRequest = buildRequest(financialInstitutionId, appId);

        GetEncryptedSecretsResponse encryptedSecrets =
                internalSecretsServiceStub.getEncryptedSecrets(getSecretsRequest);

        return encryptedSecrets.getEncryptedSecretsList().stream()
                .collect(Collectors.toMap(TppSecret::getKey, TppSecret::getValue));
    }

    private GetSecretsRequest buildRequest(String financialInstitutionId, String appId) {
        return GetSecretsRequest.newBuilder()
                .setFinancialInstitutionId(financialInstitutionId)
                .setAppId(appId)
                .build();
    }

    private SslContext buildSslContext(TppSecretsServiceConfiguration configuration) {
        File caCertPath = getCaCertPath(configuration);
        SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient().trustManager(caCertPath);

        switch (configuration.getCertificatesLocation()) {
            case CLUSTER:
                if (configuration.getTlsCrtPath() != null
                        && configuration.getTlsKeyPath() != null) {
                    sslContextBuilder.keyManager(
                            new File(configuration.getTlsCrtPath()),
                            new File(configuration.getTlsKeyPath()));
                } else {
                    throw new IllegalStateException(
                            "Missing client authentication key and/or certificate for inside "
                                    + "cluster operation of Secrets Service");
                }
                break;

            case HOME_P12:
                File clientP12File =
                        new File(System.getProperty("user.home"), "/.eidas/eidas_client.p12");

                try {
                    ByteArrayInputStream clientCertificateStream =
                            new ByteArrayInputStream(Files.toByteArray(clientP12File));

                    KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
                    keyStore.load(
                            clientCertificateStream, LOCAL_CLIENT_P12_FILE_PASS.toCharArray());

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
                                    + "authentication to Secrets Service running in stating environment",
                            e);
                }
                break;

            case HOME_PEM:
                File localClientCertFile =
                        new File(System.getProperty("user.home"), "/.eidas/local-cluster/tls.crt");
                File localClientKeyFile =
                        new File(System.getProperty("user.home"), "/.eidas/local-cluster/tls.key");
                if (!localClientCertFile.exists() || !localClientKeyFile.exists()) {
                    throw getLocalClusterTlsMaFilesNotAvailableException();
                }
                sslContextBuilder.keyManager(localClientCertFile, localClientKeyFile);
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

            case HOME_P12:
                return new File("data/eidas_dev_certificates/aggregation-staging-ca.pem");

            case HOME_PEM:
                File localCaCertFile =
                        new File(System.getProperty("user.home"), "/.eidas/local-cluster/ca.crt");
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
                        + "/.eidas/local-cluster/ with the following names: ca.crt, tls.key, tls.crt");
    }
}
