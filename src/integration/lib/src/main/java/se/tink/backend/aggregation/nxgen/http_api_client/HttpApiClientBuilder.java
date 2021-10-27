package se.tink.backend.aggregation.nxgen.http_api_client;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.constants.CommonHeaders;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;
import se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage.ClientIdDetector;
import se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage.ConsentIdDetector;
import se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage.TokenDetector;
import se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage.VariableDetector;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.ClientConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.ClientConfiguration.ClientConfigurationBuilder;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.Configuration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.EidasConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.EidasConfiguration.EidasConfigurationBuilder;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.LoggingConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.ServiceConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.configuration.TlsConfiguration;
import se.tink.libraries.aggregation_agent_api_client.src.http.HttpApiClient;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableKey;

@Getter
@Setter
@Accessors(chain = true)
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
    private HttpAapLogger httpAapLogger;
    private PersistentStorage persistentStorage;
    private Map<String, Object> secretsConfiguration;
    private String userIp;
    private String mockServerUrl;
    private String userAgent = CommonHeaders.DEFAULT_USER_AGENT;
    private AggregatorInfo aggregator;

    public static HttpApiClientBuilder builder() {
        return new HttpApiClientBuilder();
    }

    public HttpApiClient build() {
        HttpApiClient httpApiClient =
                new HttpApiClient(
                        Configuration.builder()
                                .clientConfiguration(buildClientConfiguration())
                                .eidasConfiguration(setupEidasConfig())
                                .loggingConfiguration(
                                        LoggingConfiguration.builder()
                                                .maskingFunction(this.logMasker::mask)
                                                .outputStream(
                                                        this.httpAapLogger.getLoggingPrintStream())
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

        populateVariablesFromSecrets(httpApiClient);
        populateVariablesFromStorage(httpApiClient);

        Optional.ofNullable(userIp)
                .ifPresent(
                        userIpAddr ->
                                httpApiClient.addVariable(VariableKey.PSU_IP_ADDRESS, userIpAddr));

        return httpApiClient;
    }

    private ClientConfiguration buildClientConfiguration() {
        ClientConfigurationBuilder builder = ClientConfiguration.builder().userAgent(userAgent);

        Optional.ofNullable(aggregator)
                .map(AggregatorInfo::getAggregatorIdentifier)
                .ifPresent(
                        aggregatorIdentifier ->
                                builder.staticHeader("X-Aggregator", aggregatorIdentifier));

        if (mockServerUrl != null) {
            builder.mockServerUrl(mockServerUrl);
        }
        return builder.build();
    }

    private void populateVariablesFromSecrets(HttpApiClient client) {
        Optional.ofNullable(secretsConfiguration.get(AgentConfigurationController.REDIRECT_URL_KEY))
                .ifPresent(
                        redirectUrl -> client.addVariable(VariableKey.REDIRECT_URI, redirectUrl));

        Optional.ofNullable(secretsConfiguration.get(AgentConfigurationController.QSEALC_KEY))
                .ifPresent(qsealc -> client.addVariable(VariableKey.SIGNATURE_CERTIFICATE, qsealc));

        Optional.ofNullable(secretsConfiguration.get("apiKey"))
                .ifPresent(apiKey -> client.addVariable(VariableKey.API_KEY, apiKey));

        Optional.ofNullable(secretsConfiguration.get("clientId"))
                .ifPresent(clientId -> client.addVariable(VariableKey.CLIENT_ID, clientId));

        Optional.ofNullable(secretsConfiguration.get("clientSecret"))
                .ifPresent(
                        clientSecret ->
                                client.addVariable(VariableKey.CLIENT_SECRET, clientSecret));
    }

    private void populateVariablesFromStorage(HttpApiClient client) {
        for (Entry<String, String> entry : persistentStorage.entrySet()) {
            String storageKey = entry.getKey();
            String storageValue = entry.getValue();

            if (storageKey.startsWith(ROTATED_STORAGE_PREFIX)) {
                continue;
            }

            for (VariableDetector detector : VARIABLE_DETECTORS) {
                boolean variableDetected =
                        detector.detectVariableFromStorage(client, storageKey, storageValue);

                if (variableDetected) {
                    break;
                }
            }
        }
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

        if (shouldUseEidasProxy()) {
            eidasConfigurationBuilder.proxyService(
                    ServiceConfiguration.builder().host(eidasHost).port(444).build());
        }

        return eidasConfigurationBuilder.build();
    }

    private boolean shouldUseEidasProxy() {
        return useEidasProxy && mockServerUrl == null;
    }

    private TlsConfiguration getTlsConfiguration(
            InternalEidasProxyConfiguration internalEidasProxyConfiguration) {
        try {
            return TlsConfiguration.fromKeyStore(
                            internalEidasProxyConfiguration.getClientCertKeystore(),
                            "changeme",
                            "clientcert")
                    .serverCertificateAuthority(
                            new String(
                                    internalEidasProxyConfiguration.getRootCa(),
                                    StandardCharsets.UTF_8))
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
