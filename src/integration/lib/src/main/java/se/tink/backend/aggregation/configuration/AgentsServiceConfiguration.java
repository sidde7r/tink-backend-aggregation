package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentsServiceConfiguration {
    @JsonProperty
    private AbnAmroConfiguration abnAmroStaging = new AbnAmroConfiguration();

    @JsonProperty
    private AbnAmroConfiguration abnAmroProduction = new AbnAmroConfiguration();

    @JsonProperty
    private AbnAmroConfiguration abnAmro = new AbnAmroConfiguration();

    @JsonProperty
    private IntegrationsConfiguration integrations = new IntegrationsConfiguration();

    @JsonProperty
    private AggregationWorkerConfiguration aggregationWorker = new AggregationWorkerConfiguration();

    @JsonProperty
    private SignatureKeyPair signatureKeyPair = new SignatureKeyPair();

    // This key pair should only be used to create the JWT for the third party callback
    @JsonProperty
    private CallbackJwtSignatureKeyPair callbackJwtSignatureKeyPair = new CallbackJwtSignatureKeyPair();

    @JsonProperty
    private ExcludedDebugClusters excludedDebugClusters = new ExcludedDebugClusters();

    @JsonProperty("creditsafe")
    private CreditSafeConfiguration creditSafe = new CreditSafeConfiguration();

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
}
