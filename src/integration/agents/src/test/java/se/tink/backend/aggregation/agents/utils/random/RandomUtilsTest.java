package se.tink.backend.aggregation.agents.utils.random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;

public class RandomUtilsTest {

    @Test
    public void shouldGenerateDoubleInSpecificRange() {
        // given
        double min = 0;
        double max = 100;

        // when
        double doubleInRange = RandomUtils.generateRandomDoubleInRange(min, max);

        // then
        assertThat(doubleInRange).isBetween(min, max);
    }

    @Test
    public void shouldThrowExceptionWhenMinIsGreaterThanMaxValue() {
        // given
        double min = 0;
        double max = 100;

        // when
        ThrowingCallable throwingCallable = () -> RandomUtils.generateRandomDoubleInRange(max, min);

        // then
        assertThatThrownBy(throwingCallable)
                .hasNoCause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bound must be greater than origin");
    }
}
