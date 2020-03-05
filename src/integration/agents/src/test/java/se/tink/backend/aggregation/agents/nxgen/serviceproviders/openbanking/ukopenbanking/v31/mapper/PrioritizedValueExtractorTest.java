package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import com.google.common.collect.ImmutableList;
import java.util.NoSuchElementException;
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
    public void shouldThrowException_whenNoMatchingValueIsFound() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                valueExtractor.pickByValuePriority(
                                        ImmutableList.of("MYSZO", "JELEN"),
                                        Function.identity(),
                                        ImmutableList.of("ZAGROZONY", "WYGINIECIEM")));

        assertThat(thrown).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldPickValue_basingOnPriority() {
        // when
        Integer result =
                valueExtractor.pickByValuePriority(
                        ImmutableList.of(1, 2, 3),
                        Function.identity(),
                        ImmutableList.of(4, 2, 1, 3));

        assertThat(result).isEqualTo(2);
    }

    @Test
    public void shouldUseExtractingFunction_toCompareValues() {
        // when
        String pickedString =
                valueExtractor.pickByValuePriority(
                        ImmutableList.of("a", "aa", "aaa"),
                        String::length,
                        ImmutableList.of(2, 1, 5));

        assertThat(pickedString).isEqualTo("aa");
    }
}
