package se.tink.backend.integration.tpp_secrets_service.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
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
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String LOCAL_CLIENT_P12_FILE_PASS = "changeme";
    private final InternalSecretsServiceGrpc.InternalSecretsServiceBlockingStub
            internalSecretsServiceStub;

    public TppSecretsServiceClient(TppSecretsServiceConfiguration configuration) {
        SslContext sslContext = buildSslContext(configuration);

        ManagedChannel channel =
                NettyChannelBuilder.forAddress(configuration.getHost(), configuration.getPort())
                        .useTransportSecurity()
                        .sslContext(sslContext)
                        .build();

        internalSecretsServiceStub = InternalSecretsServiceGrpc.newBlockingStub(channel);
    }

    public Map<String, String> getAllSecrets(String financialInstitutionId, String appId) {
        GetSecretsRequest getSecretsRequest = buildRequest(financialInstitutionId, appId);

        GetAllSecretsResponse allSecretsResponse =
                internalSecretsServiceStub.getAllSecrets(getSecretsRequest);

        List<TppSecret> allSecretsList = allSecretsResponse.getEncryptedSecretsList();
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

        if (configuration.getTlsCrtPath() != null && configuration.getTlsKeyPath() != null) {
            sslContextBuilder.keyManager(
                    new File(configuration.getTlsCrtPath()),
                    new File(configuration.getTlsKeyPath()));
        } else if (configuration.isLocalTppSecretsDev()) {

            File clientP12File =
                    new File(System.getProperty("user.home"), "/.eidas/eidas_client.p12");

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
                throw new IllegalStateException(e);
            }
        } else {
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
        if (configuration.getCaPath() != null) {
            return new File(configuration.getCaPath());
        } else if (configuration.isLocalTppSecretsDev()) {
            // Running in local development, we can trust aggregation staging
            return new File("data/eidas_dev_certificates/aggregation-staging-ca.pem");
        } else {
            throw new IllegalStateException("Trusted CA for Tpp Secrets Service not configured");
        }
    }
}
