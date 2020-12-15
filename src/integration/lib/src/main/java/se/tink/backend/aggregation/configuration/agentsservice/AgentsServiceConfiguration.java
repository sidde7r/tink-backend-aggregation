package se.tink.backend.aggregation.configuration.agentsservice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.integration.agent_data_availability_tracker.common.configuration.AgentDataAvailabilityTrackerConfiguration;
import se.tink.backend.integration.tpp_secrets_service.client.configuration.TppSecretsServiceConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentsServiceConfiguration {
    @JsonProperty private AbnAmroConfiguration abnAmroStaging = new AbnAmroConfiguration();

    @JsonProperty private AbnAmroConfiguration abnAmroProduction = new AbnAmroConfiguration();

    @JsonProperty private AbnAmroConfiguration abnAmro = new AbnAmroConfiguration();

    @JsonProperty private IntegrationsConfiguration integrations = new IntegrationsConfiguration();

    @JsonProperty
    private AggregationWorkerConfiguration aggregationWorker = new AggregationWorkerConfiguration();

    @JsonProperty private SignatureKeyPair signatureKeyPair = new SignatureKeyPair();

    // This key pair should only be used to create the JWT for the third party callback
    @JsonProperty
    private CallbackJwtSignatureKeyPair callbackJwtSignatureKeyPair =
            new CallbackJwtSignatureKeyPair();

    @JsonProperty private ExcludedDebugClusters excludedDebugClusters = new ExcludedDebugClusters();

    @JsonProperty("creditsafe")
    private CreditSafeConfiguration creditSafe = new CreditSafeConfiguration();

    @JsonProperty private EidasProxyConfiguration eidasProxy = new EidasProxyConfiguration();

    @JsonProperty
    private AgentDataAvailabilityTrackerConfiguration agentDataAvailabilityTrackerConfiguration =
            new AgentDataAvailabilityTrackerConfiguration(null, 0, null);

    @JsonProperty
    private TppSecretsServiceConfiguration tppSecretsServiceConfiguration =
            new TppSecretsServiceConfiguration();

    @JsonProperty private TestConfiguration testConfiguration = new TestConfiguration();

    @JsonProperty private Map<String, Boolean> featureFlags = new HashMap<>();

    @JsonProperty
    private Map<String, PasswordBasedProxyConfiguration> countryProxies = new HashMap<>();

    public TppSecretsServiceConfiguration getTppSecretsServiceConfiguration() {
        return tppSecretsServiceConfiguration;
    }

    public AbnAmroConfiguration getAbnAmroStaging() {
        return abnAmroStaging;
    }

    public AbnAmroConfiguration getAbnAmroProduction() {
        return abnAmroProduction;
    }

    public AbnAmroConfiguration getAbnAmro() {
        return abnAmro;
    }

    public IntegrationsConfiguration getIntegrations() {
        return integrations;
    }

    public AggregationWorkerConfiguration getAggregationWorker() {
        return aggregationWorker;
    }

    public SignatureKeyPair getSignatureKeyPair() {
        return signatureKeyPair;
    }

    public CallbackJwtSignatureKeyPair getCallbackJwtSignatureKeyPair() {
        return callbackJwtSignatureKeyPair;
    }

    public ExcludedDebugClusters getExcludedDebugClusters() {
        return excludedDebugClusters;
    }

    public CreditSafeConfiguration getCreditSafe() {
        return creditSafe;
    }

    public EidasProxyConfiguration getEidasProxy() {
        return eidasProxy;
    }

    public AgentDataAvailabilityTrackerConfiguration
            getAgentDataAvailabilityTrackerConfiguration() {
        return agentDataAvailabilityTrackerConfiguration;
    }

    public TestConfiguration getTestConfiguration() {
        return testConfiguration;
    }

    @JsonIgnore
    public boolean isFeatureEnabled(String featureName) {
        return featureFlags.getOrDefault(featureName, false);
    }

    @JsonIgnore
    public PasswordBasedProxyConfiguration getCountryProxy(String country) {
        return getCountryProxy(country, 0);
    }

    @JsonIgnore
    public PasswordBasedProxyConfiguration getCountryProxy(String country, int seed) {
        final List<String> countryKeys =
                countryProxies.keySet().stream()
                        .filter(key -> key.startsWith(country))
                        .collect(Collectors.toList());
        Preconditions.checkArgument(
                countryKeys.size() > 0, "No proxies configured for country '%s'", country);
        return countryProxies.get(countryKeys.get(Math.abs(seed) % countryKeys.size()));
    }
}
