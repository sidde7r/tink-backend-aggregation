package se.tink.libraries.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;

public class PrioritizedValueExtractorTest {

    private PrioritizedValueExtractor valueExtractor;

    @Before
    public void setUp() {
        valueExtractor = new PrioritizedValueExtractor();
    }

    @Test
    public void shouldReturnEmpty_whenNoMatchingValueIsFound() {
        Optional<String> ret =
                valueExtractor.pickByValuePriority(
                        ImmutableList.of("ONE", "TWO"),
                        Function.identity(),
                        ImmutableList.of("THREE", "FOUR"));

        assertThat(ret.isPresent()).isFalse();
    }

    @Test
    public void shouldPickValue_basingOnPriority() {
        Optional<Integer> result =
                valueExtractor.pickByValuePriority(
                        ImmutableList.of(1, 2, 3),
                        Function.identity(),
                        ImmutableList.of(4, 2, 1, 3));

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(2);
    }

    @Test
    public void shouldUseExtractingFunction_toCompareValues() {
        Optional<String> pickedString =
                valueExtractor.pickByValuePriority(
                        ImmutableList.of("a", "aa", "aaa"),
                        String::length,
                        ImmutableList.of(2, 1, 5));

        assertThat(pickedString.isPresent()).isTrue();
        assertThat(pickedString.get()).isEqualTo("aa");
    }
}
