package se.tink.backend.aggregation.rpc;

import java.util.Set;
import se.tink.backend.agents.rpc.Provider;

public final class SecretsNamesValidationRequest {
    private Provider provider;
    private Set<String> secretsNames;
    private Set<String> excludedSecretsNames;
    private Set<String> sensitiveSecretsNames;
    private Set<String> excludedSensitiveSecretsNames;

    public SecretsNamesValidationRequest(
            Provider provider,
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> sensitiveSecretsNames,
            Set<String> excludedSensitiveSecretsNames) {
        this.provider = provider;
        this.secretsNames = secretsNames;
        this.excludedSecretsNames = excludedSecretsNames;
        this.sensitiveSecretsNames = sensitiveSecretsNames;
        this.excludedSensitiveSecretsNames = excludedSensitiveSecretsNames;
    }

    public Provider getProvider() {
        return provider;
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
