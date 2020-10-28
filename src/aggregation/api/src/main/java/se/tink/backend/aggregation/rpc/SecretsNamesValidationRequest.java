package se.tink.backend.aggregation.rpc;

import java.util.Set;

public final class SecretsNamesValidationRequest {
    private String financialInstitutionId;
    private String providerId;
    private Set<String> secretsNames;
    private Set<String> excludedSecretsNames;
    private Set<String> sensitiveSecretsNames;
    private Set<String> excludedSensitiveSecretsNames;
    private Set<String> agentConfigParamNames;
    private Set<String> excludedAgentConfigParamNames;

    public SecretsNamesValidationRequest() {}

    public SecretsNamesValidationRequest(
            String financialInstitutionId,
            String providerId,
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> sensitiveSecretsNames,
            Set<String> excludedSensitiveSecretsNames,
            Set<String> agentConfigParamNames,
            Set<String> excludedAgentConfigParamNames) {
        this.financialInstitutionId = financialInstitutionId;
        this.providerId = providerId;
        this.secretsNames = secretsNames;
        this.excludedSecretsNames = excludedSecretsNames;
        this.sensitiveSecretsNames = sensitiveSecretsNames;
        this.excludedSensitiveSecretsNames = excludedSensitiveSecretsNames;
        this.agentConfigParamNames = agentConfigParamNames;
        this.excludedAgentConfigParamNames = excludedAgentConfigParamNames;
    }

    public String getFinancialInstitutionId() {
        return financialInstitutionId;
    }

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
}
