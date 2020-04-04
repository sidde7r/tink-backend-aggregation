package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.tools.validator.builder.ClientConfigurationValidatorBuilderForTest;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationResponse;

public class BunqClientConfigurationValidatorTest {
    private ClientConfigurationValidatorBuilderForTest clientConfigurationValidatorBuilderForTest;

    @Before
    public void setup() {
        clientConfigurationValidatorBuilderForTest =
                new ClientConfigurationValidatorBuilderForTest.Builder("nl", "nl-bunq-oauth2")
                        .build();
    }

    @Test
    public void ClientConfigurationValidationCorrectValidationTest() {
        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("clientId")
                        .add("psd2InstallationKeyPair")
                        .build();
        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("psd2ApiKey")
                        .add("psd2ClientAuthToken")
                        .add("clientSecret")
                        .build();
        Set<String> agentConfigParamNames =
                ImmutableSet.<String>builder().add("redirectUrls").build();

        Set<String> excludedSecretsNames = Collections.emptySet();
        Set<String> excludedSensitiveSecretsNames = Collections.emptySet();
        Set<String> excludedAgentConfigParamNames = Collections.emptySet();

        SecretsNamesValidationResponse response =
                clientConfigurationValidatorBuilderForTest
                        .getClientConfigurationValidator()
                        .validate(
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

    @Test
    public void ClientConfigurationValidationInvalidValidationTest() {
        Set<String> secretsNames =
                ImmutableSet.<String>builder().add("clientId").add("invalidSecret").build();
        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("psd2ApiKey")
                        .add("invalidSensitiveSecret1")
                        .add("invalidSensitiveSecret2")
                        .build();

        Set<String> agentConfigParamNames =
                ImmutableSet.<String>builder()
                        .add("redirectUrls")
                        .add("invalidAgentConfigParam1")
                        .add("invalidAgentConfigParam2")
                        .build();

        Set<String> excludedSecretsNames = Collections.emptySet();
        Set<String> excludedSensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("psd2ClientAuthToken")
                        .add("invalidSensitiveSecret2")
                        .build();
        Set<String> excludedAgentConfigParamNames =
                ImmutableSet.<String>builder().add("invalidAgentConfigParam2").build();

        SecretsNamesValidationResponse response =
                clientConfigurationValidatorBuilderForTest
                        .getClientConfigurationValidator()
                        .validate(
                                secretsNames,
                                excludedSecretsNames,
                                sensitiveSecretsNames,
                                excludedSensitiveSecretsNames,
                                agentConfigParamNames,
                                excludedAgentConfigParamNames);

        assertThat(response.isValid()).isEqualTo(false);
        assertThat(response.getMissingSecretsNames()).containsExactly("psd2InstallationKeyPair");
        assertThat(response.getMissingSensitiveSecretsNames()).containsExactly("clientSecret");
        assertThat(response.getMissingAgentConfigParamNames()).isEmpty();
        assertThat(response.getInvalidSecretsNames()).containsExactly("invalidSecret");
        assertThat(response.getInvalidSensitiveSecretsNames())
                .containsExactly("invalidSensitiveSecret1");
        assertThat(response.getInvalidAgentConfigParamNames())
                .containsExactly("invalidAgentConfigParam1");
        assertThat(response.getValidationResultMessage())
                .isEqualTo(
                        "Secrets are wrong.\n"
                                + "The following secrets should not be present : [invalidSecret]\n"
                                + "The following secrets are missing : [psd2InstallationKeyPair]\n"
                                + "The following sensitive secrets should not be present : [invalidSensitiveSecret1]\n"
                                + "The following sensitive secrets are missing : [clientSecret]\n"
                                + "The following agent config parameters should not be present : [invalidAgentConfigParam1]\n");
    }
}
