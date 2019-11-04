package se.tink.backend.aggregation.rpc;

import java.util.Set;

public final class SecretsNamesValidationResponse {
    private Set<String> invalidSecretsNames;
    private Set<String> missingSecretsNames;
    private Set<String> invalidSensitiveSecretsNames;
    private Set<String> missingSensitiveSecretsNames;

    public SecretsNamesValidationResponse(
            Set<String> invalidSecretsNames,
            Set<String> missingSecretsNames,
            Set<String> invalidSensitiveSecretsNames,
            Set<String> missingSensitiveSecretsNames) {
        this.invalidSecretsNames = invalidSecretsNames;
        this.missingSecretsNames = missingSecretsNames;
        this.invalidSensitiveSecretsNames = invalidSensitiveSecretsNames;
        this.missingSensitiveSecretsNames = missingSensitiveSecretsNames;
    }
}
