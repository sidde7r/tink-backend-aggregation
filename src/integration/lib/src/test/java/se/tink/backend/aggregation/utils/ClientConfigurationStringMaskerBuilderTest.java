package se.tink.backend.aggregation.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class ClientConfigurationStringMaskerBuilderTest {

    @Test
    public void testClientConfigurationMaskBuilder() {
        ClientConfigurationStringMaskerBuilder masker =
                new ClientConfigurationStringMaskerBuilder(
                        ImmutableList.of("SECRET", "CLIENT ID", "SEC"));
        ImmutableList<String> valuesToMask = masker.getValuesToMask();
        assertThat(valuesToMask).containsExactly("CLIENT ID", "SECRET", "SEC");
    }
}
