package se.tink.backend.aggregation.agents.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.Set;
import org.junit.Test;

public class ClientConfigurationValidatorTest {
    private ClientConfigurationValidator clientConfigurationValidator;

    @Test
    public void getMissingSecretsFieldsTest() {
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

        clientConfigurationValidator =
                new ClientConfigurationValidator(mockClientConfigurationMetaInfoHandler);

        Set<String> secretsNames =
                ImmutableSet.<String>builder().add("secret1").add("redirectUrls").build();
        Set<String> missingSecretsFields =
                clientConfigurationValidator.getMissingSecretsFields(
                        secretsNames, Collections.emptySet());

        assertThat(missingSecretsFields).contains("secret2");

        Set<String> missingSecretsFieldsWithExcluded =
                clientConfigurationValidator.getMissingSecretsFields(
                        secretsNames, ImmutableSet.of("secret2"));

        assertThat(missingSecretsFieldsWithExcluded).isEmpty();
    }
}
