package se.tink.backend.aggregation.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import org.junit.Test;
import se.tink.backend.aggregation.utils.masker.SensitiveValuesCollectionMaskerPatternsProvider;

public class SensitiveValuesCollectionMaskerPatternsProviderTest {

    @Test
    public void testClientConfigurationMaskBuilder() {
        SensitiveValuesCollectionMaskerPatternsProvider masker =
                new SensitiveValuesCollectionMaskerPatternsProvider(
                        ImmutableList.of("SECRET", "CLIENT ID", "SEC"));
        ImmutableList<String> valuesToMask =
                masker.getPatternsToMask().stream()
                        .map(Pattern::toString)
                        .collect(ImmutableList.toImmutableList());
        assertThat(valuesToMask).containsExactly("CLIENT ID", "SECRET", "SEC");
    }
}
