package se.tink.backend.aggregation.agents.tools.response;

import java.util.Set;

public class ClientConfigurationBlendedSecretsValidationResponse {
    private boolean valid;
    private Set<String> invalidBlendedSecretsNames;
    private Set<String> missingBlendedSecretsNames;
    private String validationResultMessage;

    public ClientConfigurationBlendedSecretsValidationResponse(
            Set<String> invalidBlendedSecretsNames, Set<String> missingBlendedSecretsNames) {
        this.valid = invalidBlendedSecretsNames.isEmpty() && missingBlendedSecretsNames.isEmpty();
        this.invalidBlendedSecretsNames = invalidBlendedSecretsNames;
        this.missingBlendedSecretsNames = missingBlendedSecretsNames;
        this.validationResultMessage = assembleValidationResultMessage();
    }

    public boolean isValid() {
        return valid;
    }

    public Set<String> getInvalidBlendedSecretsNames() {
        return invalidBlendedSecretsNames;
    }

    public Set<String> getMissingBlendedSecretsNames() {
        return missingBlendedSecretsNames;
    }

    public String getValidationResultMessage() {
        return validationResultMessage;
    }

    private String assembleValidationResultMessage() {
        if (valid) {
            return "Secrets names validated correctly.";
        } else {
            StringBuffer sb = new StringBuffer("Secrets are wrong.\n");
            if (!invalidBlendedSecretsNames.isEmpty()) {
                sb.append(
                        "The following secrets should not be present : "
                                + invalidBlendedSecretsNames.toString()
                                + "\n");
            }
            if (!missingBlendedSecretsNames.isEmpty()) {
                sb.append(
                        "The following secrets are missing : "
                                + missingBlendedSecretsNames.toString()
                                + "\n");
            }
            return sb.toString();
        }
    }
}
