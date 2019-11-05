package se.tink.backend.aggregation.agents.tools;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
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
        Set<String> invalidSecretsFields =
                getInvalidSecretsFields(
                        request.getSecretsNames(), request.getExcludedSecretsNames());
        Set<String> missingSecretsFields =
                getMissingSecretsFields(
                        request.getSecretsNames(), request.getExcludedSecretsNames());

        Set<String> invalidSensitiveSecretsFields =
                getInvalidSensitiveSecretsFields(
                        request.getSensitiveSecretsNames(),
                        request.getExcludedSensitiveSecretsNames());
        Set<String> missingSensitiveSecretsFields =
                getMissingSensitiveSecretsFields(
                        request.getSensitiveSecretsNames(),
                        request.getExcludedSensitiveSecretsNames());

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

        return getMissingSecretsFields(
                secretsNames, excludedSecretsNames, secretFieldsNamesFromConfigurationClass);
    }

    // Package private for testing.
    Set<String> getMissingSensitiveSecretsFields(
            Set<String> sensitiveSecretsNames, Set<String> excludedSensitiveSecretsNames) {
        Set<String> sensitiveSecretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames();

        return getMissingSecretsFields(
                sensitiveSecretsNames,
                excludedSensitiveSecretsNames,
                sensitiveSecretFieldsNamesFromConfigurationClass);
    }

    // Package private for testing.
    Set<String> getInvalidSensitiveSecretsFields(
            Set<String> sensitiveSecretsNames, Set<String> excludedSensitiveSecretsNames) {
        Set<String> sensitiveSecretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames();

        return getInvalidSecretsFields(
                sensitiveSecretsNames,
                excludedSensitiveSecretsNames,
                sensitiveSecretFieldsNamesFromConfigurationClass);
    }

    // Package private for testing.
    Set<String> getInvalidSecretsFields(
            Set<String> secretsNames, Set<String> excludedSecretsNames) {
        Set<String> secretFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getSecretFieldsNames();

        return getInvalidSecretsFields(
                secretsNames, excludedSecretsNames, secretFieldsNamesFromConfigurationClass);
    }

    private Set<String> getMissingSecretsFields(
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

    private Set<String> getInvalidSecretsFields(
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
