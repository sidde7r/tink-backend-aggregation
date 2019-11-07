package se.tink.backend.aggregation.rpc;

import java.util.Set;
import se.tink.backend.agents.rpc.Provider;

public final class SecretsNamesValidationRequest {
    private String financialInstitutionId;
    private Set<String> secretsNames;
    private Set<String> excludedSecretsNames;
    private Set<String> sensitiveSecretsNames;
    private Set<String> excludedSensitiveSecretsNames;
    private Set<String> agentConfigParamNames;
    private Set<String> excludedAgentConfigParamNames;

    public SecretsNamesValidationRequest() {}

    public SecretsNamesValidationRequest(
            String financialInstitutionId,
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> sensitiveSecretsNames,
            Set<String> excludedSensitiveSecretsNames,
            Set<String> agentConfigParamNames,
            Set<String> excludedAgentConfigParamNames) {
        this.financialInstitutionId = financialInstitutionId;
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
