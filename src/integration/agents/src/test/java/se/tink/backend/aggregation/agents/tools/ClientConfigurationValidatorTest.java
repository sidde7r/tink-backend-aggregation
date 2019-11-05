package se.tink.backend.aggregation.agents.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationRequest;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationResponse;

public class ClientConfigurationValidatorTest {
    private ClientConfigurationValidator clientConfigurationValidator;

    @Test
    public void getMissingSecretsFieldsTestWithMissingFields() {
        clientConfigurationValidator = getClientConfigurationValidatorForTestWithMissingFields();

        Set<String> secretsNames =
                ImmutableSet.<String>builder().add("secret1").add("redirectUrls").build();
        Set<String> missingSecretsFields =
                clientConfigurationValidator.getMissingSecretsFields(
                        secretsNames, Collections.emptySet());

        assertThat(missingSecretsFields).containsExactly("secret2");

        Set<String> missingSecretsFieldsWithExcluded =
                clientConfigurationValidator.getMissingSecretsFields(
                        secretsNames, ImmutableSet.of("secret2"));

        assertThat(missingSecretsFieldsWithExcluded).isEmpty();
    }

    @Test
    public void getMissingSecretsFieldsTestWithInvalidFields() {
        clientConfigurationValidator = getClientConfigurationValidatorForTestWithInvalidFields();

        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("redirectUrls")
                        .build();
        Set<String> missingSecretsFields =
                clientConfigurationValidator.getMissingSecretsFields(
                        secretsNames, Collections.emptySet());

        assertThat(missingSecretsFields).isEmpty();
    }

    @Test
    public void getInvalidSecretsFieldsTestWithMissingFields() {
        clientConfigurationValidator = getClientConfigurationValidatorForTestWithMissingFields();

        Set<String> secretsNames =
                ImmutableSet.<String>builder().add("secret1").add("redirectUrls").build();
        Set<String> invalidSecretsFields =
                clientConfigurationValidator.getInvalidSecretsFields(
                        secretsNames, Collections.emptySet());

        assertThat(invalidSecretsFields).isEmpty();
    }

    @Test
    public void getInvalidSecretsFieldsTestWithInvalidFields() {
        clientConfigurationValidator = getClientConfigurationValidatorForTestWithInvalidFields();

        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("redirectUrls")
                        .build();
        Set<String> invalidSecretsFields =
                clientConfigurationValidator.getInvalidSecretsFields(
                        secretsNames, Collections.emptySet());

        assertThat(invalidSecretsFields).containsExactly("secret2");

        Set<String> invalidSecretsFieldsWithExcluded =
                clientConfigurationValidator.getInvalidSecretsFields(
                        secretsNames, ImmutableSet.of("secret2"));

        assertThat(invalidSecretsFieldsWithExcluded).isEmpty();
    }

    @Test
    public void validateTestWithMissingFields() {
        clientConfigurationValidator =
                getClientConfigurationValidatorForValidateTestWithMissingFields();

        SecretsNamesValidationRequest request = getSecretsNamesValidationRequest();

        SecretsNamesValidationResponse response = clientConfigurationValidator.validate(request);

        assertThat(response.isValid()).isEqualTo(false);
        assertThat(response.getInvalidSecretsNames()).isEmpty();
        assertThat(response.getInvalidSensitiveSecretsNames()).isEmpty();
        assertThat(response.getMissingSecretsNames()).containsExactly("secret4");
        assertThat(response.getMissingSensitiveSecretsNames()).containsExactly("sensitiveSecret4");
    }

    @Test
    public void validateTestWithInvalidFields() {
        clientConfigurationValidator =
                getClientConfigurationValidatorForValidateTestWithInvalidFields();

        SecretsNamesValidationRequest request = getSecretsNamesValidationRequest();

        SecretsNamesValidationResponse response = clientConfigurationValidator.validate(request);

        assertThat(response.isValid()).isEqualTo(false);
        assertThat(response.getMissingSecretsNames()).isEmpty();
        assertThat(response.getMissingSensitiveSecretsNames()).isEmpty();
        assertThat(response.getInvalidSecretsNames()).containsExactly("secret2");
        assertThat(response.getInvalidSensitiveSecretsNames()).containsExactly("sensitiveSecret2");
    }

    @Test
    public void validateTestWithCorrectFields() {
        clientConfigurationValidator =
                getClientConfigurationValidatorForValidateTestWithCorrectFields();

        SecretsNamesValidationRequest request = getSecretsNamesValidationRequest();

        SecretsNamesValidationResponse response = clientConfigurationValidator.validate(request);

        assertThat(response.isValid()).isEqualTo(true);
        assertThat(response.getMissingSecretsNames()).isEmpty();
        assertThat(response.getMissingSensitiveSecretsNames()).isEmpty();
        assertThat(response.getInvalidSecretsNames()).isEmpty();
        assertThat(response.getInvalidSensitiveSecretsNames()).isEmpty();
    }

    private SecretsNamesValidationRequest getSecretsNamesValidationRequest() {
        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("secret1")
                        .add("secret2")
                        .add("secret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSecretsNames =
                ImmutableSet.<String>builder().add("secret3").add("secret5").build();

        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("sensitiveSecret1")
                        .add("sensitiveSecret2")
                        .add("sensitiveSecret3")
                        .add("redirectUrls")
                        .build();

        Set<String> excludedSensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("sensitiveSecret3")
                        .add("sensitiveSecret5")
                        .build();

        return new SecretsNamesValidationRequest(
                null,
                secretsNames,
                excludedSecretsNames,
                sensitiveSecretsNames,
                excludedSensitiveSecretsNames);
    }

    private ClientConfigurationValidator getClientConfigurationValidatorForTestWithMissingFields() {
        ClientConfigurationMetaInfoHandler mockClientConfigurationMetaInfoHandler =
                mock(ClientConfigurationMetaInfoHandler.class);
        when(mockClientConfigurationMetaInfoHandler.getSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder()
                                .add("secret1")
                                .add("secret2")
                                .add("redirectUrl")
                                .build());
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
                .thenCallRealMethod();

        return new ClientConfigurationValidator(mockClientConfigurationMetaInfoHandler);
    }

    private ClientConfigurationValidator getClientConfigurationValidatorForTestWithInvalidFields() {
        ClientConfigurationMetaInfoHandler mockClientConfigurationMetaInfoHandler =
                mock(ClientConfigurationMetaInfoHandler.class);
        when(mockClientConfigurationMetaInfoHandler.getSecretFieldsNames())
                .thenReturn(
                        ImmutableSet.<String>builder().add("secret1").add("redirectUrl").build());
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
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
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
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
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
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
        when(mockClientConfigurationMetaInfoHandler.mapSpecialConfigClassFieldNames(any(Set.class)))
                .thenCallRealMethod();

        return new ClientConfigurationValidator(mockClientConfigurationMetaInfoHandler);
    }
}
