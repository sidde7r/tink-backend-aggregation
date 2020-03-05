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
        // given
        ImmutableList<String> input = ImmutableList.of("MYSZO", "JELEN");
        ImmutableList<String> priorityList = ImmutableList.of("ZAGROZONY", "WYGINIECIEM");
        ImmutableList<String> emptyPriorityList = ImmutableList.of();

        // when
        Throwable thrown =
                catchThrowable(
                        () -> {
                            valueExtractor.pickByValuePriority(
                                    input, Function.identity(), priorityList);
                        });

        // then
        assertThat(thrown).isInstanceOf(NoSuchElementException.class);

        // when
        Throwable anotherThrown =
                catchThrowable(
                        () ->
                                valueExtractor.pickByValuePriority(
                                        input, Function.identity(), emptyPriorityList));

        // then
        assertThat(anotherThrown).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldPickValue_basingOnPriority() {
        // given
        ImmutableList<Integer> input = ImmutableList.of(1, 2, 3);
        ImmutableList<Integer> priorityList = ImmutableList.of(4, 2, 1, 3);

        // when
        Integer result =
                valueExtractor.pickByValuePriority(input, Function.identity(), priorityList);

        // then
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void shouldUseExtractingFunction_toCompareValues() {
        // given
        ImmutableList<String> input = ImmutableList.of("a", "aa", "aaa");
        ImmutableList<Integer> priorityList = ImmutableList.of(2, 1, 5);

        // when
        String pickedString =
                valueExtractor.pickByValuePriority(input, String::length, priorityList);

        // then
        assertThat(pickedString).isEqualTo("aa");
    }

    @Test
    public void shouldThrowException_whenComparedValuesFromInputCollectionAreDuplicated() {
        // given
        ImmutableList<String> input =
                ImmutableList.of("someValue", "duplicatedValue", "duplicatedValue", "anotherValue");
        ImmutableList<String> priorityList = ImmutableList.of("anotherValue", "abc123");

        // when
        Throwable thrown =
                catchThrowable(
                        () -> {
                            valueExtractor.pickByValuePriority(
                                    input, Function.identity(), priorityList);
                        });

        // then
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }
}
