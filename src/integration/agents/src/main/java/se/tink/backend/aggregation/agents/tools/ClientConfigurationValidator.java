package se.tink.backend.aggregation.agents.tools;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationRequest;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationResponse;

public class ClientConfigurationValidator {
    private final ClientConfigurationMetaInfoHandler clientConfigurationMetaInfoHandler;

    public ClientConfigurationValidator(Provider provider) {
        this.clientConfigurationMetaInfoHandler = new ClientConfigurationMetaInfoHandler(provider);
    }

    // Package private for testing.
    ClientConfigurationValidator(
            ClientConfigurationMetaInfoHandler clientConfigurationMetaInfoHandler) {
        this.clientConfigurationMetaInfoHandler = clientConfigurationMetaInfoHandler;
    }

    public SecretsNamesValidationResponse validate(SecretsNamesValidationRequest request) {
        Set<String> mappedSecretsNames =
                clientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(
                        request.getSecretsNames());
        Set<String> invalidSecretsFields = getInvalidSecretsFields(mappedSecretsNames);
        Set<String> missingSecretsFields =
                getMissingSecretsFields(mappedSecretsNames, request.getExcludedSecretsNames());

        Set<String> mappedSensitiveSecretsNames =
                clientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(
                        request.getSensitiveSecretsNames());
        Set<String> invalidSensitiveSecretsFields =
                getInvalidSensitiveSecretsFields(mappedSensitiveSecretsNames);
        Set<String> missingSensitiveSecretsFields =
                getMissingSensitiveSecretsFields(mappedSensitiveSecretsNames);

        return new SecretsNamesValidationResponse(
                invalidSecretsFields,
                missingSecretsFields,
                invalidSensitiveSecretsFields,
                missingSensitiveSecretsFields);
    }

    // Package private for testing.
    Set<String> getMissingSecretsFields(
            Set<String> secretsNames, Set<String> excludedSecretsNames) {
        Set<String> secretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSecretFieldsNames();
        if (secretFieldsNamesFromConfigurationClass.containsAll(secretsNames)) {
            return Collections.emptySet();
        } else {
            return secretFieldsNamesFromConfigurationClass.stream()
                    // Just get the ones that are in the configuration class and not in the set that
                    // we passed to the method.
                    .filter(
                            secretFieldNameFromConfiguration ->
                                    !secretsNames.contains(secretFieldNameFromConfiguration))
                    // Do not count as missing those that are excluded.
                    .filter(
                            missingSecretFieldNameFromConfiguration ->
                                    !excludedSecretsNames.contains(
                                            missingSecretFieldNameFromConfiguration))
                    .collect(Collectors.toSet());
        }
    }

    private Set<String> getInvalidSecretsFields(Set<String> secretsNames) {
        return null;
    }

    private Set<String> getMissingSensitiveSecretsFields(Set<String> sensitiveSecretsNames) {
        return null;
    }

    private Set<String> getInvalidSensitiveSecretsFields(Set<String> sensitiveSecretsNames) {
        return null;
    }
}
