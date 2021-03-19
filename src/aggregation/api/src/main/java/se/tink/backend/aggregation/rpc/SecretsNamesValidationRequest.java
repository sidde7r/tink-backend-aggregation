package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SecretsNamesValidationRequest {
    private String providerId;
    private Set<String> secretsNames;
    private Set<String> excludedSecretsNames;
    private Set<String> sensitiveSecretsNames;
    private Set<String> excludedSensitiveSecretsNames;
    private Set<String> agentConfigParamNames;
    private Set<String> excludedAgentConfigParamNames;

    public String getProviderId() {
        return providerId;
    }

    public Set<String> getSecretsNames() {
        return secretsNames;
    }

    public Set<String> getExcludedSecretsNames() {
        return excludedSecretsNames;
    }

    public Set<String> getSensitiveSecretsNames() {
        return sensitiveSecretsNames;
    }

    public Set<String> getExcludedSensitiveSecretsNames() {
        return excludedSensitiveSecretsNames;
    }

    public Set<String> getAgentConfigParamNames() {
        return agentConfigParamNames;
    }

    public Set<String> getExcludedAgentConfigParamNames() {
        return excludedAgentConfigParamNames;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setSecretsNames(Set<String> secretsNames) {
        this.secretsNames = secretsNames;
    }

    public void setExcludedSecretsNames(Set<String> excludedSecretsNames) {
        this.excludedSecretsNames = excludedSecretsNames;
    }

    public void setSensitiveSecretsNames(Set<String> sensitiveSecretsNames) {
        this.sensitiveSecretsNames = sensitiveSecretsNames;
    }

    public void setExcludedSensitiveSecretsNames(Set<String> excludedSensitiveSecretsNames) {
        this.excludedSensitiveSecretsNames = excludedSensitiveSecretsNames;
    }

    public void setAgentConfigParamNames(Set<String> agentConfigParamNames) {
        this.agentConfigParamNames = agentConfigParamNames;
    }

    public void setExcludedAgentConfigParamNames(Set<String> excludedAgentConfigParamNames) {
        this.excludedAgentConfigParamNames = excludedAgentConfigParamNames;
    }
}
