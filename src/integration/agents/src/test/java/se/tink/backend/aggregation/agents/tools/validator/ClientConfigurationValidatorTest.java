package se.tink.backend.aggregation.agents.tools.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.agents.tools.ClientConfigurationMetaInfoHandler;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationResponse;

public class ClientConfigurationValidatorTest {
    private ClientConfigurationValidator clientConfigurationValidator;

    @Test
    public void getMissingSecretsFieldsTestWithMissingFields() {
        clientConfigurationValidator = getClientConfigurationValidatorInternalMethodsTest();

        Set<String> secretsNames =
                ImmutableSet.<String>builder().add("secret1").add("redirectUrls").build();
        Set<String> mockSecretFieldsNamesFromConfigurationClass =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("redirectUrl")
                        .build();
        Set<String> missingSecretsFields =
                clientConfigurationValidator.getMissingSecretsFields(
                        secretsNames,
                        Collections.emptySet(),
                        mockSecretFieldsNamesFromConfigurationClass);

        assertThat(missingSecretsFields).containsExactly("secret2");

        Set<String> missingSecretsFieldsWithExcluded =
                clientConfigurationValidator.getMissingSecretsFields(
                        secretsNames,
                        ImmutableSet.of("secret2"),
                        mockSecretFieldsNamesFromConfigurationClass);

        assertThat(missingSecretsFieldsWithExcluded).isEmpty();
    }

    @Test
    public void getMissingSecretsFieldsTestWithInvalidFields() {
        clientConfigurationValidator = getClientConfigurationValidatorInternalMethodsTest();

        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("redirectUrls")
                        .build();
        Set<String> mockSecretFieldsNamesFromConfigurationClass =
                ImmutableSet.<String>builder().add("secret1").add("redirectUrl").build();
        Set<String> missingSecretsFields =
                clientConfigurationValidator.getMissingSecretsFields(
                        secretsNames,
                        Collections.emptySet(),
                        mockSecretFieldsNamesFromConfigurationClass);

        assertThat(missingSecretsFields).isEmpty();
    }

    @Test
    public void getInvalidSecretsFieldsTestWithMissingFields() {
        clientConfigurationValidator = getClientConfigurationValidatorInternalMethodsTest();

        Set<String> secretsNames =
                ImmutableSet.<String>builder().add("secret1").add("redirectUrls").build();
        Set<String> mockSecretFieldsNamesFromConfigurationClass =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("redirectUrl")
                        .build();
        Set<String> invalidSecretsFields =
                clientConfigurationValidator.getInvalidSecretsFields(
                        secretsNames,
                        Collections.emptySet(),
                        mockSecretFieldsNamesFromConfigurationClass);

        assertThat(invalidSecretsFields).isEmpty();
    }

    @Test
    public void getInvalidSecretsFieldsTestWithInvalidFields() {
        clientConfigurationValidator = getClientConfigurationValidatorInternalMethodsTest();

        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("redirectUrls")
                        .build();
        Set<String> mockSecretFieldsNamesFromConfigurationClass =
                ImmutableSet.<String>builder().add("secret1").add("redirectUrl").build();
        Set<String> invalidSecretsFields =
                clientConfigurationValidator.getInvalidSecretsFields(
                        secretsNames,
                        Collections.emptySet(),
                        mockSecretFieldsNamesFromConfigurationClass);

        assertThat(invalidSecretsFields).containsExactly("secret2");

        Set<String> invalidSecretsFieldsWithExcluded =
                clientConfigurationValidator.getInvalidSecretsFields(
                        secretsNames,
                        ImmutableSet.of("secret2"),
                        mockSecretFieldsNamesFromConfigurationClass);

        assertThat(invalidSecretsFieldsWithExcluded).isEmpty();
    }

    @Test
    public void validateTestWithMissingFields() {
        clientConfigurationValidator =
                getClientConfigurationValidatorForValidateTestWithMissingFields();

        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("secret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSecretsNames = ImmutableSet.<String>builder().add("secret3").build();

        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("sensitiveSecret1")
                        .add("sensitiveSecret2")
                        .add("sensitiveSecret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSensitiveSecretsNames =
                ImmutableSet.<String>builder().add("sensitiveSecret3").build();

        Set<String> agentConfigParamNames =
                ImmutableSet.<String>builder()
                        .add("agentConfigParam1")
                        .add("agentConfigParam2")
                        .add("agentConfigParam3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedAgentConfigParamNames =
                ImmutableSet.<String>builder().add("agentConfigParam3").build();

        SecretsNamesValidationResponse response =
                clientConfigurationValidator.validate(
                        secretsNames,
                        excludedSecretsNames,
                        sensitiveSecretsNames,
                        excludedSensitiveSecretsNames,
                        agentConfigParamNames,
                        excludedAgentConfigParamNames);

        assertThat(response.isValid()).isEqualTo(false);
        assertThat(response.getInvalidSecretsNames()).isEmpty();
        assertThat(response.getInvalidSensitiveSecretsNames()).isEmpty();
        assertThat(response.getInvalidAgentConfigParamNames()).isEmpty();
        assertThat(response.getMissingSecretsNames()).containsExactly("secret4");
        assertThat(response.getMissingSensitiveSecretsNames()).containsExactly("sensitiveSecret4");
        assertThat(response.getMissingAgentConfigParamNames()).containsExactly("agentConfigParam4");
        assertThat(response.getValidationResultMessage())
                .isEqualTo(
                        "Secrets are wrong.\n"
                                + "The following secrets are missing : [secret4]\n"
                                + "The following sensitive secrets are missing : [sensitiveSecret4]\n"
                                + "The following agent config parameters are missing : [agentConfigParam4]\n");
    }

    @Test
    public void validateTestWithInvalidFields() {
        clientConfigurationValidator =
                getClientConfigurationValidatorForValidateTestWithInvalidFields();

        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("secret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSecretsNames = ImmutableSet.<String>builder().add("secret3").build();

        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("sensitiveSecret1")
                        .add("sensitiveSecret2")
                        .add("sensitiveSecret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSensitiveSecretsNames =
                ImmutableSet.<String>builder().add("sensitiveSecret3").build();

        Set<String> agentConfigParamNames =
                ImmutableSet.<String>builder()
                        .add("agentConfigParam1")
                        .add("agentConfigParam2")
                        .add("agentConfigParam3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedAgentConfigParamNames =
                ImmutableSet.<String>builder().add("agentConfigParam3").build();

        SecretsNamesValidationResponse response =
                clientConfigurationValidator.validate(
                        secretsNames,
                        excludedSecretsNames,
                        sensitiveSecretsNames,
                        excludedSensitiveSecretsNames,
                        agentConfigParamNames,
                        excludedAgentConfigParamNames);

        assertThat(response.isValid()).isEqualTo(false);
        assertThat(response.getMissingSecretsNames()).isEmpty();
        assertThat(response.getMissingSensitiveSecretsNames()).isEmpty();
        assertThat(response.getMissingAgentConfigParamNames()).isEmpty();
        assertThat(response.getInvalidSecretsNames()).containsExactly("secret2");
        assertThat(response.getInvalidSensitiveSecretsNames()).containsExactly("sensitiveSecret2");
        assertThat(response.getInvalidAgentConfigParamNames()).containsExactly("agentConfigParam2");
        assertThat(response.getValidationResultMessage())
                .isEqualTo(
                        "Secrets are wrong.\n"
                                + "The following secrets should not be present : [secret2]\n"
                                + "The following sensitive secrets should not be present : [sensitiveSecret2]\n"
                                + "The following agent config parameters should not be present : [agentConfigParam2]\n");
    }

    @Test
    public void validateTestWithMissingAndInvalidFields() {
        clientConfigurationValidator =
                getClientConfigurationValidatorForValidateTestWithMissingAndInvalidFields();

        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("secret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSecretsNames = ImmutableSet.<String>builder().add("secret3").build();

        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("sensitiveSecret1")
                        .add("sensitiveSecret2")
                        .add("sensitiveSecret3")
                        .build();

        Set<String> excludedSensitiveSecretsNames =
                ImmutableSet.<String>builder().add("sensitiveSecret3").add("redirectUrl").build();

        Set<String> agentConfigParamNames =
                ImmutableSet.<String>builder()
                        .add("agentConfigParam1")
                        .add("agentConfigParam2")
                        .add("agentConfigParam3")
                        .build();

        Set<String> excludedAgentConfigParamNames =
                ImmutableSet.<String>builder().add("agentConfigParam3").add("redirectUrls").build();

        SecretsNamesValidationResponse response =
                clientConfigurationValidator.validate(
                        secretsNames,
                        excludedSecretsNames,
                        sensitiveSecretsNames,
                        excludedSensitiveSecretsNames,
                        agentConfigParamNames,
                        excludedAgentConfigParamNames);

        assertThat(response.isValid()).isEqualTo(false);
        assertThat(response.getMissingSecretsNames()).containsExactly("secret4");
        assertThat(response.getMissingSensitiveSecretsNames()).containsExactly("sensitiveSecret4");
        assertThat(response.getMissingAgentConfigParamNames()).containsExactly("agentConfigParam4");
        assertThat(response.getInvalidSecretsNames()).containsExactly("secret2");
        assertThat(response.getInvalidSensitiveSecretsNames()).containsExactly("sensitiveSecret2");
        assertThat(response.getInvalidAgentConfigParamNames()).containsExactly("agentConfigParam2");
        assertThat(response.getValidationResultMessage())
                .isEqualTo(
                        "Secrets are wrong.\n"
                                + "The following secrets should not be present : [secret2]\n"
                                + "The following secrets are missing : [secret4]\n"
                                + "The following sensitive secrets should not be present : [sensitiveSecret2]\n"
                                + "The following sensitive secrets are missing : [sensitiveSecret4]\n"
                                + "The following agent config parameters should not be present : [agentConfigParam2]\n"
                                + "The following agent config parameters are missing : [agentConfigParam4]\n");
    }

    @Test
    public void validateTestWithCorrectFields() {
        clientConfigurationValidator =
                getClientConfigurationValidatorForValidateTestWithCorrectFields();

        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("secret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSecretsNames = ImmutableSet.<String>builder().add("secret3").build();

        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("sensitiveSecret1")
                        .add("sensitiveSecret2")
                        .add("sensitiveSecret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSensitiveSecretsNames =
                ImmutableSet.<String>builder().add("sensitiveSecret3").build();

        Set<String> agentConfigParamNames =
                ImmutableSet.<String>builder()
                        .add("agentConfigParam1")
                        .add("agentConfigParam2")
                        .add("agentConfigParam3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedAgentConfigParamNames =
                ImmutableSet.<String>builder().add("agentConfigParam3").build();

        SecretsNamesValidationResponse response =
                clientConfigurationValidator.validate(
                        secretsNames,
                        excludedSecretsNames,
                        sensitiveSecretsNames,
                        excludedSensitiveSecretsNames,
                        agentConfigParamNames,
                        excludedAgentConfigParamNames);

        assertThat(response.isValid()).isEqualTo(true);
        assertThat(response.getMissingSecretsNames()).isEmpty();
        assertThat(response.getMissingSensitiveSecretsNames()).isEmpty();
        assertThat(response.getMissingAgentConfigParamNames()).isEmpty();
        assertThat(response.getInvalidSecretsNames()).isEmpty();
        assertThat(response.getInvalidSensitiveSecretsNames()).isEmpty();
        assertThat(response.getInvalidAgentConfigParamNames()).isEmpty();
        assertThat(response.getValidationResultMessage())
                .isEqualTo("Secrets names validated correctly.");
    }

    private ClientConfigurationValidator getClientConfigurationValidatorInternalMethodsTest() {
        ClientConfigurationMetaInfoHandler mockClientConfigurationMetaInfoHandler =
                mock(ClientConfigurationMetaInfoHandler.class);

        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
                .thenCallRealMethod();
        when(mockClientConfigurationMetaInfoHandler.inverseMapSpecialConfigClassFieldNames(
                        any(Set.class)))
                .thenCallRealMethod();

        return new ClientConfigurationValidator(mockClientConfigurationMetaInfoHandler);
    }

    private ClientConfigurationValidator
            getClientConfigurationValidatorForValidateTestWithCorrectFields() {
        ClientConfigurationMetaInfoHandler mockClientConfigurationMetaInfoHandler =
                mock(ClientConfigurationMetaInfoHandler.class);
        when(mockClientConfigurationMetaInfoHandler.getSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("secret1")
                                .add("secret2")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("sensitiveSecret1")
                                .add("sensitiveSecret2")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.getAgentConfigParamFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("agentConfigParam1")
                                .add("agentConfigParam2")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
                .thenCallRealMethod();
        when(mockClientConfigurationMetaInfoHandler.inverseMapSpecialConfigClassFieldNames(
                        any(Set.class)))
                .thenCallRealMethod();

        return new ClientConfigurationValidator(mockClientConfigurationMetaInfoHandler);
    }

    private ClientConfigurationValidator
            getClientConfigurationValidatorForValidateTestWithMissingFields() {
        ClientConfigurationMetaInfoHandler mockClientConfigurationMetaInfoHandler =
                mock(ClientConfigurationMetaInfoHandler.class);
        when(mockClientConfigurationMetaInfoHandler.getSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("secret1")
                                .add("secret2")
                                .add("secret4")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("sensitiveSecret1")
                                .add("sensitiveSecret2")
                                .add("sensitiveSecret4")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.getAgentConfigParamFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("agentConfigParam1")
                                .add("agentConfigParam2")
                                .add("agentConfigParam4")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
                .thenCallRealMethod();
        when(mockClientConfigurationMetaInfoHandler.inverseMapSpecialConfigClassFieldNames(
                        any(Set.class)))
                .thenCallRealMethod();

        return new ClientConfigurationValidator(mockClientConfigurationMetaInfoHandler);
    }

    private ClientConfigurationValidator
            getClientConfigurationValidatorForValidateTestWithInvalidFields() {
        ClientConfigurationMetaInfoHandler mockClientConfigurationMetaInfoHandler =
                mock(ClientConfigurationMetaInfoHandler.class);
        when(mockClientConfigurationMetaInfoHandler.getSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder().add("secret1").add("redirectUrl").build());
        when(mockClientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("sensitiveSecret1")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.getAgentConfigParamFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("agentConfigParam1")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
                .thenCallRealMethod();
        when(mockClientConfigurationMetaInfoHandler.inverseMapSpecialConfigClassFieldNames(
                        any(Set.class)))
                .thenCallRealMethod();

        return new ClientConfigurationValidator(mockClientConfigurationMetaInfoHandler);
    }

    private ClientConfigurationValidator
            getClientConfigurationValidatorForValidateTestWithMissingAndInvalidFields() {
        ClientConfigurationMetaInfoHandler mockClientConfigurationMetaInfoHandler =
                mock(ClientConfigurationMetaInfoHandler.class);
        when(mockClientConfigurationMetaInfoHandler.getSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("secret1")
                                .add("secret4")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.getSensitiveSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("sensitiveSecret1")
                                .add("sensitiveSecret4")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.getAgentConfigParamFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("agentConfigParam1")
                                .add("agentConfigParam4")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
                .thenCallRealMethod();
        when(mockClientConfigurationMetaInfoHandler.inverseMapSpecialConfigClassFieldNames(
                        any(Set.class)))
                .thenCallRealMethod();

        return new ClientConfigurationValidator(mockClientConfigurationMetaInfoHandler);
    }
}
