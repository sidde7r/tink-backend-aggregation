package se.tink.backend.integration.tpp_secrets_service.client;

import com.google.inject.Inject;
import io.grpc.StatusException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;
import se.tink.backend.secretservice.grpc.CertificateResponse;
import se.tink.backend.secretservice.grpc.GetAllSecretsResponse;
import se.tink.backend.secretsservice.client.SecretsServiceInternalClient;
import se.tink.backend.secretsservice.client.SecretsServiceInternalClientImpl;
import se.tink.backend.secretsservice.client.configuration.GrpcClientConfig;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;
import se.tink.libraries.grpc_tink_request_id_tracing_interceptor.TinkRequestIdClientTracingInterceptor;
import se.tink.libraries.tracing.grpc.interceptor.ClientTracingInterceptor;

@Slf4j
public final class TppSecretsServiceInternalClientImpl extends ManagedSafeStop
        implements ManagedTppSecretsServiceInternalClient {
    private SecretsServiceInternalClient secretsServiceInternalClient;
    private GrpcClientConfig grpcClientConfig;

    @Inject
    public TppSecretsServiceInternalClientImpl(
            // will migrate to constructor with GrpcClientConfig variable ...
            TppSecretsServiceConfiguration tppSecretsServiceConfiguration) {
        try {
            // for test envs, CertificatesLocation is not set in config when ss client is not
            // enabled.
            // will migrate this ...
            if (tppSecretsServiceConfiguration.isEnabled()) {
                this.grpcClientConfig =
                        GrpcClientConfig.builder()
                                .withGrpcPort(tppSecretsServiceConfiguration.getPort())
                                .withGrpcHost(tppSecretsServiceConfiguration.getHost())
                                .withCaCert(tppSecretsServiceConfiguration.getCaPath())
                                .withTlsClientCert(tppSecretsServiceConfiguration.getTlsCrtPath())
                                .withTlsClientKey(tppSecretsServiceConfiguration.getTlsKeyPath())
                                .withUseClientAuth(tppSecretsServiceConfiguration.isEnabled())
                                .withPlaintext(!tppSecretsServiceConfiguration.isEnabled())
                                .withCertificatesLocation(
                                        se.tink.backend.secretsservice.client.configuration
                                                .CertificatesLocation.fromString(
                                                tppSecretsServiceConfiguration
                                                        .getCertificatesLocation()
                                                        .toString()))
                                .withEnabledRetryPolicy(
                                        tppSecretsServiceConfiguration.isEnabledRetryPolicy())
                                .build();
            } else {
                this.grpcClientConfig = null;
            }
        } catch (StatusException e) {
            throw new IllegalStateException(
                    "Failed to transform tppSecretsServiceConfiguration to grpcClientConfig", e);
        }
    }

    @Override
    public void start() {
        // use the flag isUseClientAuth to represent if secrets service client is enabled
        if (grpcClientConfig != null && grpcClientConfig.isUseClientAuth()) {
            secretsServiceInternalClient =
                    new SecretsServiceInternalClientImpl(
                            grpcClientConfig,
                            new ClientTracingInterceptor(),
                            new TinkRequestIdClientTracingInterceptor());
        } else {
            log.warn(
                    "Trying to start an instance of TppSecretsServiceInternalClientImpl when the configuration says it is not enabled.");
        }
    }

    @Override
    public synchronized void doStop() {
        // use the flag isUseClientAuth to represent if secrets service client is enabled
        if (grpcClientConfig != null && grpcClientConfig.isUseClientAuth()) {
            shutDown();
        } else {
            log.warn(
                    "Trying to stop an instance of TppSecretsServiceInternalClientImpl when the configuration says it is not enabled.");
        }
    }

    @Override
    public CertificateResponse fetchQwacCertificate(
            String clusterId, String appId, String certId, String providerId) {
        return secretsServiceInternalClient.fetchQwacCertificate(
                clusterId, appId, certId, providerId);
    }

    @Override
    public CertificateResponse fetchQsealcCertificate(
            String clusterId, String appId, String certId, String providerId) {
        return secretsServiceInternalClient.fetchQsealcCertificate(
                clusterId, appId, certId, providerId);
    }

    @Override
    public GetAllSecretsResponse getAllSecrets(
            String clusterId, String appId, String certId, String providerId) {
        return secretsServiceInternalClient.getAllSecrets(clusterId, appId, certId, providerId);
    }

    @Override
    public Optional<String> getLicenseModel(String clusterId, String appId, String providerId) {
        return secretsServiceInternalClient.getLicenseModel(clusterId, appId, providerId);
    }

    @Override
    public void ping() {
        secretsServiceInternalClient.ping();
    }

    @Override
    public void shutDown() {
        secretsServiceInternalClient.shutDown();
    }
}
