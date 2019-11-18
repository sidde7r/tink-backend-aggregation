package se.tink.backend.aggregation.rpc;

import java.util.Set;

public final class SecretsNamesValidationRequest {
    private String financialInstitutionId;
    private Set<String> secretsNames;
    private Set<String> excludedSecretsNames;
    private Set<String> sensitiveSecretsNames;
    private Set<String> excludedSensitiveSecretsNames;

    public SecretsNamesValidationRequest() {}

    public SecretsNamesValidationRequest(
            String financialInstitutionId,
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> sensitiveSecretsNames,
            Set<String> excludedSensitiveSecretsNames) {
        this.financialInstitutionId = financialInstitutionId;
        this.secretsNames = secretsNames;
        this.excludedSecretsNames = excludedSecretsNames;
        this.sensitiveSecretsNames = sensitiveSecretsNames;
        this.excludedSensitiveSecretsNames = excludedSensitiveSecretsNames;
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
}
