package se.tink.backend.aggregation.rpc;

import java.util.Set;

public final class SecretsNamesValidationResponse {
    private boolean valid;
    private Set<String> invalidSecretsNames;
    private Set<String> missingSecretsNames;
    private Set<String> invalidSensitiveSecretsNames;
    private Set<String> missingSensitiveSecretsNames;

    public SecretsNamesValidationResponse(
            Set<String> invalidSecretsNames,
            Set<String> missingSecretsNames,
            Set<String> invalidSensitiveSecretsNames,
            Set<String> missingSensitiveSecretsNames) {
        this.valid =
                invalidSecretsNames.isEmpty()
                        && missingSecretsNames.isEmpty()
                        && invalidSecretsNames.isEmpty()
                        && missingSensitiveSecretsNames.isEmpty();
        this.invalidSecretsNames = invalidSecretsNames;
        this.missingSecretsNames = missingSecretsNames;
        this.invalidSensitiveSecretsNames = invalidSensitiveSecretsNames;
        this.missingSensitiveSecretsNames = missingSensitiveSecretsNames;
    }

    public boolean isValid() {
        return valid;
    }

    public Set<String> getInvalidSecretsNames() {
        return invalidSecretsNames;
    }

    public Set<String> getMissingSecretsNames() {
        return missingSecretsNames;
    }

    public Set<String> getInvalidSensitiveSecretsNames() {
        return invalidSensitiveSecretsNames;
    }

    public Set<String> getMissingSensitiveSecretsNames() {
        return missingSensitiveSecretsNames;
    }

    public String getValidationResultMessage() {
        if (valid) {
            return "Secrets names validated correctly.";
        } else {
            StringBuffer sb = new StringBuffer("Secrets are wrong.\n");
            if (!invalidSecretsNames.isEmpty()) {
                sb.append(
                        "The following secrets should not be present : "
                                + invalidSecretsNames.toString()
                                + "\n");
            }
            if (!missingSecretsNames.isEmpty()) {
                sb.append(
                        "The following secrets are missing : "
                                + missingSecretsNames.toString()
                                + "\n");
            }
            if (!invalidSensitiveSecretsNames.isEmpty()) {
                sb.append(
                        "The following sensitive secrets should not be present : "
                                + invalidSensitiveSecretsNames.toString()
                                + "\n");
            }
            if (!missingSensitiveSecretsNames.isEmpty()) {
                sb.append(
                        "The following sensitive secrets are missing : "
                                + missingSensitiveSecretsNames.toString()
                                + "\n");
            }
            return sb.toString();
        }
    }
}
