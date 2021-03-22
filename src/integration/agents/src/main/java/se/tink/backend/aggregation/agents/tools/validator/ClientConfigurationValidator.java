package se.tink.backend.aggregation.agents.tools.validator;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.tools.ClientConfigurationMetaInfoHandler;
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

    public SecretsNamesValidationResponse validate(
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> sensitiveSecretsNames,
            Set<String> excludedSensitiveSecretsNames,
            Set<String> agentConfigParamNames,
            Set<String> excludedAgentConfigParamNames) {
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

        Set<String> invalidAgentConfigParamFields =
                getInvalidAgentConfigParamFields(
                        agentConfigParamNames, excludedAgentConfigParamNames);
        Set<String> missingAgentConfigParamFields =
                getMissingAgentConfigParamFields(
                        agentConfigParamNames, excludedAgentConfigParamNames);

        return new SecretsNamesValidationResponse(
                invalidSecretsFields,
                missingSecretsFields,
                invalidSensitiveSecretsFields,
                missingSensitiveSecretsFields,
                invalidAgentConfigParamFields,
                missingAgentConfigParamFields,
                "");
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

    private Set<String> getMissingAgentConfigParamFields(
            Set<String> agentConfigParamNames, Set<String> excludedAgentConfigParamNames) {
        Set<String> agentConfigParamFieldsFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getAgentConfigParamFieldsNames();

        return getMissingSecretsFields(
                agentConfigParamNames,
                excludedAgentConfigParamNames,
                agentConfigParamFieldsFieldsNamesFromConfigurationClass);
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

    private Set<String> getInvalidAgentConfigParamFields(
            Set<String> agentConfigParamNames, Set<String> excludedAgentConfigParamNames) {
        Set<String> agentConfigParamFieldsFieldsNamesFromConfigurationClass =
                clientConfigurationMetaInfoHandler.getAgentConfigParamFieldsNames();

        return getInvalidSecretsFields(
                agentConfigParamNames,
                excludedAgentConfigParamNames,
                agentConfigParamFieldsFieldsNamesFromConfigurationClass);
    }

    // Package private for testing.
    Set<String> getMissingSecretsFields(
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> secretFieldsNamesFromConfigurationClass) {
        Set<String> mappedSecretsNames =
                clientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(secretsNames);
        Set<String> mappedExcludedSecretsNames =
                clientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(
                        excludedSecretsNames);

        ImmutableSet<String> missingSecretsFields =
                secretFieldsNamesFromConfigurationClass.stream()
                        // Just get the ones that are in the configuration class and not in the set
                        // that
                        // we passed to the method.
                        .filter(
                                secretFieldNameFromConfiguration ->
                                        !mappedSecretsNames.contains(
                                                secretFieldNameFromConfiguration))
                        // Do not count as missing those that are excluded.
                        .filter(
                                missingSecretFieldNameFromConfiguration ->
                                        !mappedExcludedSecretsNames.contains(
                                                missingSecretFieldNameFromConfiguration))
                        .collect(ImmutableSet.toImmutableSet());

        return clientConfigurationMetaInfoHandler.inverseMapSpecialConfigClassFieldNames(
                missingSecretsFields);
    }

    // Package private for testing.
    Set<String> getInvalidSecretsFields(
            Set<String> secretsNames,
            Set<String> excludedSecretsNames,
            Set<String> secretFieldsNamesFromConfigurationClass) {
        Set<String> mappedSecretsNames =
                clientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(secretsNames);
        Set<String> mappedExcludedSecretsNames =
                clientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(
                        excludedSecretsNames);

        ImmutableSet<String> invalidSecretsFields =
                mappedSecretsNames.stream()
                        // We pick those that we are trying to validate and are not found in the
                        // ClientConfiguration class.
                        .filter(
                                secretFieldNameToValidate ->
                                        !secretFieldsNamesFromConfigurationClass.contains(
                                                secretFieldNameToValidate))
                        // Do not count as invalid those that are excluded from validation.
                        .filter(
                                invalidSecretFieldName ->
                                        !mappedExcludedSecretsNames.contains(
                                                invalidSecretFieldName))
                        .collect(ImmutableSet.toImmutableSet());

        return clientConfigurationMetaInfoHandler.inverseMapSpecialConfigClassFieldNames(
                invalidSecretsFields);
    }
}
