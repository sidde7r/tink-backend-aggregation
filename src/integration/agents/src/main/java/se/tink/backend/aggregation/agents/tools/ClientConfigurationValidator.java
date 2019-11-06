package se.tink.backend.aggregation.agents.tools;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.tools.response.ClientConfigurationBlendedSecretsValidationResponse;
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

    public ClientConfigurationBlendedSecretsValidationResponse validate(
            Set<String> blendedSecrets) {
        Set<String> secretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSecretFieldsNames();
        Set<String> sensitiveSecretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames();
        Set<String> blendedSecretsNamesFromConfigurationClass =
                ImmutableSet.<String>builder()
                        .addAll(secretFieldsNamesFromConfigurationClass)
                        .addAll(sensitiveSecretFieldsNamesFromConfigurationClass)
                        .build();

        Set<String> invalidBlendedSecretsFields =
                getInvalidSecretsFields(
                        blendedSecrets,
                        Collections.emptySet(),
                        blendedSecretsNamesFromConfigurationClass);
        Set<String> missingBlendedSecretsFields =
                getMissingSecretsFields(
                        blendedSecrets,
                        Collections.emptySet(),
                        blendedSecretsNamesFromConfigurationClass);

        return new ClientConfigurationBlendedSecretsValidationResponse(
                invalidBlendedSecretsFields, missingBlendedSecretsFields);
    }

    public SecretsNamesValidationResponse validate(
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> sensitiveSecretsNames,
            Set<String> excludedSensitiveSecretsNames) {
        Set<String> invalidSecretsFields =
                getInvalidSecretsFields(secretsNames, excludedSecretsNames);
        Set<String> missingSecretsFields =
                getMissingSecretsFields(secretsNames, excludedSecretsNames);

        Set<String> invalidSensitiveSecretsFields =
                getInvalidSensitiveSecretsFields(
                        sensitiveSecretsNames, excludedSensitiveSecretsNames);
        Set<String> missingSensitiveSecretsFields =
                getMissingSensitiveSecretsFields(
                        sensitiveSecretsNames, excludedSensitiveSecretsNames);

        return new SecretsNamesValidationResponse(
                invalidSecretsFields,
                missingSecretsFields,
                invalidSensitiveSecretsFields,
                missingSensitiveSecretsFields);
    }

    private Set<String> getMissingSecretsFields(
            Set<String> secretsNames, Set<String> excludedSecretsNames) {
        Set<String> secretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSecretFieldsNames();

        return getMissingSecretsFields(
                secretsNames, excludedSecretsNames, secretFieldsNamesFromConfigurationClass);
    }

    private Set<String> getMissingSensitiveSecretsFields(
            Set<String> sensitiveSecretsNames, Set<String> excludedSensitiveSecretsNames) {
        Set<String> sensitiveSecretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames();

        return getMissingSecretsFields(
                sensitiveSecretsNames,
                excludedSensitiveSecretsNames,
                sensitiveSecretFieldsNamesFromConfigurationClass);
    }

    private Set<String> getInvalidSensitiveSecretsFields(
            Set<String> sensitiveSecretsNames, Set<String> excludedSensitiveSecretsNames) {
        Set<String> sensitiveSecretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames();

        return getInvalidSecretsFields(
                sensitiveSecretsNames,
                excludedSensitiveSecretsNames,
                sensitiveSecretFieldsNamesFromConfigurationClass);
    }

    private Set<String> getInvalidSecretsFields(
            Set<String> secretsNames, Set<String> excludedSecretsNames) {
        Set<String> secretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSecretFieldsNames();

        return getInvalidSecretsFields(
                secretsNames, excludedSecretsNames, secretFieldsNamesFromConfigurationClass);
    }

    // Package private for testing.
    Set<String> getMissingSecretsFields(
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> secretFieldsNamesFromConfigurationClass) {
        Set<String> mappedSecretsNames =
                clientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(secretsNames);

        return secretFieldsNamesFromConfigurationClass.stream()
                // Just get the ones that are in the configuration class and not in the set that
                // we passed to the method.
                .filter(
                        secretFieldNameFromConfiguration ->
                                !mappedSecretsNames.contains(secretFieldNameFromConfiguration))
                // Do not count as missing those that are excluded.
                .filter(
                        missingSecretFieldNameFromConfiguration ->
                                !excludedSecretsNames.contains(
                                        missingSecretFieldNameFromConfiguration))
                .collect(ImmutableSet.toImmutableSet());
    }

    // Package private for testing.
    Set<String> getInvalidSecretsFields(
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> secretFieldsNamesFromConfigurationClass) {
        Set<String> mappedSecretsNames =
                clientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(secretsNames);

        return mappedSecretsNames.stream()
                // We pick those that we are trying to validate and are not found in the
                // ClientConfiguration class.
                .filter(
                        secretFieldNameToValidate ->
                                !secretFieldsNamesFromConfigurationClass.contains(
                                        secretFieldNameToValidate))
                // Do not count as invalid those that are excluded from validation.
                .filter(
                        invalidSecretFieldName ->
                                !excludedSecretsNames.contains(invalidSecretFieldName))
                .collect(ImmutableSet.toImmutableSet());
    }
}
