package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.tools.ClientConfigurationValidatorBuilderForTest;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationRequest;
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
                        .add("redirectUrls")
                        .add("clientId")
                        .add("psd2InstallationKeyPair")
                        .build();
        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("psd2ApiKey")
                        .add("psd2ClientAuthToken")
                        .add("clientSecret")
                        .build();

        Set<String> excludedSecretsNames = Collections.emptySet();
        Set<String> excludedSensitiveSecretsNames = Collections.emptySet();

        SecretsNamesValidationRequest request =
                new SecretsNamesValidationRequest(
                        clientConfigurationValidatorBuilderForTest.getProvider(),
                        secretsNames,
                        excludedSecretsNames,
                        sensitiveSecretsNames,
                        excludedSensitiveSecretsNames);

        SecretsNamesValidationResponse response =
                clientConfigurationValidatorBuilderForTest
                        .getClientConfigurationValidator()
                        .validate(request);

        assertThat(response.isValid()).isEqualTo(true);
        assertThat(response.getMissingSecretsNames()).isEmpty();
        assertThat(response.getMissingSensitiveSecretsNames()).isEmpty();
        assertThat(response.getInvalidSecretsNames()).isEmpty();
        assertThat(response.getInvalidSensitiveSecretsNames()).isEmpty();
        assertThat(response.getValidationResultMessage())
                .isEqualTo("Secrets names validated correctly.");
    }

    @Test
    public void ClientConfigurationValidationInvalidValidationTest() {
        Set<String> secretsNames =
                ImmutableSet.<String>builder()
                        .add("redirectUrls")
                        .add("clientId")
                        .add("invalidSecret")
                        .build();
        Set<String> sensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("psd2ApiKey")
                        .add("invalidSensitiveSecret1")
                        .add("invalidSensitiveSecret2")
                        .build();

        Set<String> excludedSecretsNames = Collections.emptySet();
        Set<String> excludedSensitiveSecretsNames =
                ImmutableSet.<String>builder()
                        .add("psd2ClientAuthToken")
                        .add("invalidSensitiveSecret2")
                        .build();

        SecretsNamesValidationRequest request =
                new SecretsNamesValidationRequest(
                        clientConfigurationValidatorBuilderForTest.getProvider(),
                        secretsNames,
                        excludedSecretsNames,
                        sensitiveSecretsNames,
                        excludedSensitiveSecretsNames);

        SecretsNamesValidationResponse response =
                clientConfigurationValidatorBuilderForTest
                        .getClientConfigurationValidator()
                        .validate(request);

        assertThat(response.isValid()).isEqualTo(false);
        assertThat(response.getMissingSecretsNames()).containsExactly("psd2InstallationKeyPair");
        assertThat(response.getMissingSensitiveSecretsNames()).containsExactly("clientSecret");
        assertThat(response.getInvalidSecretsNames()).containsExactly("invalidSecret");
        assertThat(response.getInvalidSensitiveSecretsNames())
                .containsExactly("invalidSensitiveSecret1");
        assertThat(response.getValidationResultMessage())
                .isEqualTo(
                        "Secrets are wrong.\n"
                                + "The following secrets should not be present : [invalidSecret]\n"
                                + "The following secrets are missing : [psd2InstallationKeyPair]\n"
                                + "The following sensitive secrets should not be present : [invalidSensitiveSecret1]\n"
                                + "The following sensitive secrets are missing : [clientSecret]\n");
    }
}
