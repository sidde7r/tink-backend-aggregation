package se.tink.backend.aggregation.nxgen.http_api_client;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.List;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage.ClientIdDetector;
import se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage.ConsentIdDetector;
import se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage.TokenDetector;
import se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage.VariableDetector;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.Configuration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.EidasConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.EidasConfiguration.EidasConfigurationBuilder;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.LoggingConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.ServiceConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.TlsConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.http.HttpApiClient;

public class HttpApiClientBuilder {
    private static final String ROTATED_STORAGE_PREFIX = "OLD_";
    private static final List<VariableDetector> VARIABLE_DETECTORS =
            ImmutableList.<VariableDetector>builder()
                    .add(new TokenDetector())
                    .add(new ConsentIdDetector())
                    .add(new ClientIdDetector())
                    .build();

    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;
    private boolean useEidasProxy;
    private LogMasker logMasker;
    private OutputStream logOutputStream;
    private PersistentStorage persistentStorage;

    public static HttpApiClientBuilder builder() {
        return new HttpApiClientBuilder();
    }

    public HttpApiClientBuilder setEidasProxyConfiguration(
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
        return this;
    }

    public HttpApiClientBuilder setEidasIdentity(EidasIdentity eidasIdentity) {
        this.eidasIdentity = eidasIdentity;
        return this;
    }

    public HttpApiClientBuilder setUseEidasProxy(boolean useEidasProxy) {
        this.useEidasProxy = useEidasProxy;
        return this;
    }

    public HttpApiClientBuilder setLogMasker(LogMasker logMasker) {
        this.logMasker = logMasker;
        return this;
    }

    public HttpApiClientBuilder setLogOutputStream(OutputStream logOutputStream) {
        this.logOutputStream = logOutputStream;
        return this;
    }

    public HttpApiClientBuilder setPersistentStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
        return this;
    }

    public HttpApiClient build() {
        HttpApiClient httpApiClient =
                new HttpApiClient(
                        Configuration.builder()
                                .eidasConfiguration(setupEidasConfig())
                                .loggingConfiguration(
                                        LoggingConfiguration.builder()
                                                .maskingFunction(this.logMasker::mask)
                                                .outputStream(this.logOutputStream)
                                                .build())
                                .build());

        this.persistentStorage.subscribeOnInsertion(
                data -> {
                    String storageKey = data.getKey();
                    Object storageValue = data.getValue();

                    if (storageKey.startsWith(ROTATED_STORAGE_PREFIX)) {
                        return;
                    }

                    for (VariableDetector detector : VARIABLE_DETECTORS) {
                        boolean variableDetected =
                                detector.detectVariableFromInsertion(
                                        httpApiClient, storageKey, storageValue);

                        if (variableDetected) {
                            break;
                        }
                    }
                });

        return httpApiClient;
    }

    private EidasConfiguration setupEidasConfig() {
        InternalEidasProxyConfiguration internalEidasProxyConfiguration =
                eidasProxyConfiguration.toInternalConfig();

        TlsConfiguration tlsConfiguration = getTlsConfiguration(internalEidasProxyConfiguration);

        String eidasHost = removeSchema(internalEidasProxyConfiguration.getHost());

        EidasConfigurationBuilder eidasConfigurationBuilder =
                EidasConfiguration.builder()
                        .appId(eidasIdentity.getAppId())
                        .certificateId(eidasIdentity.getCertId())
                        .clusterId(eidasIdentity.getClusterId())
                        .requesterAgentClass(eidasIdentity.getRequester())
                        .requesterProviderId(eidasIdentity.getProviderId())
                        .tlsConfiguration(tlsConfiguration)
                        .signingService(
                                ServiceConfiguration.builder().host(eidasHost).port(443).build());

        if (useEidasProxy) {
            eidasConfigurationBuilder.proxyService(
                    ServiceConfiguration.builder().host(eidasHost).port(444).build());
        }

        return eidasConfigurationBuilder.build();
    }

    private TlsConfiguration getTlsConfiguration(
            InternalEidasProxyConfiguration internalEidasProxyConfiguration) {
        try {
            return TlsConfiguration.fromKeyStore(
                            internalEidasProxyConfiguration.getClientCertKeystore(),
                            "changeme",
                            "clientcert")
                    .serverCertificateAuthority(
                            new String(internalEidasProxyConfiguration.getRootCa()))
                    .build();
        } catch (KeyStoreException
                | IOException
                | NoSuchProviderException
                | CertificateException
                | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not create TLS configuration", e);
        }
    }

    private String removeSchema(String value) {
        return value.replaceFirst("^https://", "");
    }
}
