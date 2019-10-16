package se.tink.backend.aggregation.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class ClientConfigurationStringMaskerTest {

    @Test
    public void testClientConfigurationMasking() {
        ClientConfigurationStringMasker masker =
                new ClientConfigurationStringMasker(ImmutableList.of("SECRET", "CLIENT ID"));
        String unmasked =
                "fasrfoisjrjofisjeiofjsioejfSECRETdaoisjdoasijdoaisCLIENT IDdjioaosjdoiaj";
        String masked = masker.getMasked(unmasked);
        assertThat(masked).doesNotContain("CLIENT ID");
        assertThat(masked).doesNotContain("SECRET");
        assertThat(masked).contains(ClientConfigurationStringMasker.MASK);
    }
}
